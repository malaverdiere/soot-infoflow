package soot.jimple.interproc.ifds.solver.parallel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import soot.SootMethod;
import soot.jimple.interproc.ifds.EdgeFunction;
import soot.jimple.interproc.ifds.EdgeFunctions;
import soot.jimple.interproc.ifds.FlowFunction;
import soot.jimple.interproc.ifds.FlowFunctions;
import soot.jimple.interproc.ifds.IDETabulationProblem;
import soot.jimple.interproc.ifds.InterproceduralCFG;
import soot.jimple.interproc.ifds.JoinLattice;
import soot.jimple.interproc.ifds.datastructures.JumpFunctions;
import soot.jimple.interproc.ifds.datastructures.SummaryFunctions;
import soot.jimple.interproc.ifds.edgefunc.AllTop;
import soot.jimple.interproc.ifds.edgefunc.EdgeIdentity;
import soot.jimple.interproc.ifds.solver.PathEdge;
import soot.toolkits.scalar.Pair;

import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

/**
 * Solves the given {@link IDETabulationProblem} as described in the 1996 paper by Sagiv,
 * Horwitz and Reps. To solve the problem, call {@link #solve()}. Results can then be
 * queried by using {@link #resultAt(Object, Object)} and {@link #resultsAt(Object)}.
 *
 * @param <N> The type of nodes in the interprocedural control-flow graph. Typically {@link Unit}.
 * @param <D> The type of data-flow facts to be computed by the tabulation problem.
 * @param <M> The type of objects used to represent methods. Typically {@link SootMethod}.
 * @param <V> The type of values to be computed along flow edges.
 */
public class ParallelIDESolver<N,D,M,V> {
	
	private final Collection<PathEdge<N,D,M>> pathWorklist = new HashSet<PathEdge<N,D,M>>();
	
	private final List<Pair<N,D>> nodeWorkList = new LinkedList<Pair<N,D>>();

	private final JumpFunctions<N,D,V> jumpFn;
	
	private final SummaryFunctions<N,D,V> summaryFunctions = new SummaryFunctions<N,D,V>();

	private final AllTop<V> allTop;
	
	private final Table<N,D,V> val = HashBasedTable.create();	
	
	//stores summaries that were queried before they were computed
	//see CC 2010 paper by Naeem, Lhotak and Rodriguez
	private final Table<N,D,Map<N,Set<D>>> endSummary = HashBasedTable.create();

	//edges going along calls
	//see CC 2010 paper by Naeem, Lhotak and Rodriguez
	private final Table<N,D,Map<N,Set<D>>> incoming = HashBasedTable.create();

	//immutable data follows
	
	private final InterproceduralCFG<N,M> icfg;
	
	private final FlowFunctions<N,D,M> flowFunctions;

	private final Multimap<M,D> initialSeeds;

	private final EdgeFunctions<N,D,M,V> edgeFunctions;

	private final JoinLattice<V> valueLattice;
	
	private final D zeroValue;
	
	
	private Executor executor = Executors.newCachedThreadPool();
	
	class CallSiteTask implements Runnable {
		
		private PathEdge<N,D,M> edge;

		private CallSiteTask(PathEdge<N, D, M> edge) {
			this.edge = edge;
		}

		@Override
		public void run() {
			processCall(edge);
		}

		/**
		 * Lines 13-20 of the algorithm; processing a call site in the caller's context
		 * @param edge an edge whose target node resembles a method call
		 */
		private void processCall(PathEdge<N,D,M> edge) {
			EdgeFunction<V> f = jumpFunction(edge);
			D d1 = edge.factAtSource();
			N n = edge.getTarget(); // a call node; line 14...
			D d2 = edge.factAtTarget();
			Set<M> callees = icfg.getCalleesOfCallAt(n);
			List<N> returnSiteNs = icfg.getReturnSitesOfCallAt(n);
			for(M sCalledProcN: callees) { //still line 14
				FlowFunction<D> function = flowFunctions.getCallFlowFunction(n, sCalledProcN);
				Set<D> res = function.computeTargets(d2);
				for(N sP: icfg.getStartPointsOf(sCalledProcN)) {			
					for(D d3: res) {
						propagate(d3, sP, d3, EdgeIdentity.<V>v()); //line 15
		
						//line 15.1 of Naeem/Lhotak/Rodriguez
						addIncoming(sP,d3,n,d2);
						
						for(Map.Entry<N, Set<D>> entry: endSummary(sP, d3).entrySet()) {
							N eP = entry.getKey();
							for (D d4 : entry.getValue()) {						
								for(N retSiteN: icfg.getReturnSitesOfCallAt(n)) {
									FlowFunction<D> retFunction = flowFunctions.getReturnFlowFunction(n, sCalledProcN, eP, retSiteN);
									for(D d5: retFunction.computeTargets(d4)) {
										EdgeFunction<V> f4 = edgeFunctions.getCallEdgeFunction(n, d2, sCalledProcN, d3);
										EdgeFunction<V> f5 = edgeFunctions.getReturnEdgeFunction(n, sCalledProcN, eP, d4, retSiteN, d5);
										EdgeFunction<V> summaryFunction = summaryFunctions.summariesFor(n, d2, retSiteN).get(d5);			
										if(summaryFunction==null) summaryFunction = allTop; //SummaryFn initialized to all-top, see line [4] in SRH96 paper
										EdgeFunction<V> fPrime = f4.composeWith(f).composeWith(f5).joinWith(summaryFunction);
										if(!fPrime.equalTo(summaryFunction)) {
											summaryFunctions.insertFunction(n,d2,retSiteN,d5,fPrime);
										}	
									}
								}
							}
						}
					}		
				}
				
				for (N returnSiteN : returnSiteNs) {
					FlowFunction<D> callToReturnFlowFunction = flowFunctions.getCallToReturnFlowFunction(n, returnSiteN);
					for(D d3: callToReturnFlowFunction.computeTargets(d2)) {
						EdgeFunction<V> edgeFnE = edgeFunctions.getCallToReturnEdgeFunction(n, d2, returnSiteN, d3);
						propagate(d1, returnSiteN, d3, f.composeWith(edgeFnE));
					}

					Map<D,EdgeFunction<V>> d3sAndF3s = summaryFunctions.summariesFor(n, d2, returnSiteN);
					for (Map.Entry<D,EdgeFunction<V>> d3AndF3 : d3sAndF3s.entrySet()) {
						D d3 = d3AndF3.getKey();
						EdgeFunction<V> f3 = d3AndF3.getValue();
						if(f3==null) f3 = allTop; //SummaryFn initialized to all-top, see line [4] in SRH96 paper
						propagate(d1, returnSiteN, d3, f.composeWith(f.composeWith(f3)));
					}
				}
				
			}		
		}
	}
	
	class ExitNodeTask implements Runnable {

		private PathEdge<N,D,M> edge;

		private ExitNodeTask(PathEdge<N, D, M> edge) {
			this.edge = edge;
		}

		@Override
		public void run() {
			processExit(edge);
		}
		
		/**
		 * Lines 21-32 of the algorithm.	
		 */
		private void processExit(PathEdge<N,D,M> edge) {
			N n = edge.getTarget(); // an exit node; line 21...
			EdgeFunction<V> f = jumpFunction(edge);
			M methodThatNeedsSummary = icfg.getMethodOf(n);
			
			D d1 = edge.factAtSource();
			D d2 = edge.factAtTarget();
			
			for(N sP: icfg.getStartPointsOf(methodThatNeedsSummary)) {
				//line 21.1 of Naeem/Lhotak/Rodriguez
				addEndSummary(sP, d1, n, d2);
				
				for (Entry<N,Set<D>> entry: incoming(d1, sP).entrySet()) {
					//line 22
					N c = entry.getKey();
					for(N retSiteC: icfg.getReturnSitesOfCallAt(c)) {
						FlowFunction<D> retFunction = flowFunctions.getReturnFlowFunction(c, methodThatNeedsSummary,n,retSiteC);
						Set<D> targets = retFunction.computeTargets(d2);
						for(D d4: entry.getValue()) {
							//line 23
							for(D d5: targets) {
								EdgeFunction<V> f4 = edgeFunctions.getCallEdgeFunction(c, d4, icfg.getMethodOf(n), d1);
								EdgeFunction<V> f5 = edgeFunctions.getReturnEdgeFunction(c, icfg.getMethodOf(n), n, d2, retSiteC, d5);
								EdgeFunction<V> summaryFunction = summaryFunctions.summariesFor(c,d4,retSiteC).get(d5);			
								if(summaryFunction==null) summaryFunction = allTop; //SummaryFn initialized to all-top, see line [4] in SRH96 paper
								EdgeFunction<V> fPrime = f4.composeWith(f).composeWith(f5).joinWith(summaryFunction);
								if(!fPrime.equalTo(summaryFunction)) {
									summaryFunctions.insertFunction(c,d4,retSiteC,d5,fPrime);
								}						
								for(Map.Entry<D,EdgeFunction<V>> valAndFunc: jumpFn.reverseLookup(c,d4).entrySet()) {
									EdgeFunction<V> f3 = valAndFunc.getValue();
									if(!f3.equalTo(allTop)); {
										D d3 = valAndFunc.getKey();
										propagate(d3, retSiteC, d5, f3.composeWith(fPrime));
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	class NormalNodeTask implements Runnable {

		private PathEdge<N,D,M> edge;

		private NormalNodeTask(PathEdge<N, D, M> edge) {
			this.edge = edge;
		}

		@Override
		public void run() {
			processNormalFlow(edge);
		}
		
		/**
		 * Lines 33-37 of the algorithm.
		 * @param edge
		 */
		private void processNormalFlow(PathEdge<N,D,M> edge) {
			D d1 = edge.factAtSource();
			N n = edge.getTarget(); 
			D d2 = edge.factAtTarget();
			EdgeFunction<V> f = jumpFunction(edge);
			for (N m : icfg.getSuccsOf(n)) {
				FlowFunction<D> flowFunction = flowFunctions.getNormalFlowFunction(n,m);
				Set<D> res = flowFunction.computeTargets(d2);
				for (D d3 : res) {
					propagate(d1, m, d3, f.composeWith(edgeFunctions.getNormalEdgeFunction(n, d2, m, d3))); 
				}
			}
		}		
	}
	
	
	/**
	 * Creates a solver for the given problem. The solver must then be started by calling
	 * {@link #solve()}.
	 */
	public ParallelIDESolver(IDETabulationProblem<N,D,M,V> tabulationProblem) {
		this.icfg = tabulationProblem.interproceduralCFG();
		this.flowFunctions = tabulationProblem.flowFunctions();
		this.initialSeeds = tabulationProblem.initialSeeds();
		this.edgeFunctions = tabulationProblem.edgeFunctions();
		this.valueLattice = tabulationProblem.joinLattice();
		this.zeroValue = tabulationProblem.zeroValue();
		this.allTop = new AllTop<V>(valueLattice.topElement());
		this.jumpFn = new JumpFunctions<N,D,V>(allTop);
	}

	/**
	 * Runs the solver on the configured problem. This can take some time. 
	 */
	public void solve() {
		for(Entry<M,Collection<D>> seed: initialSeeds.asMap().entrySet()) {
			M entryPoint = seed.getKey();
			Collection<D> initialAbstraction = seed.getValue();
			for(N startPoint:icfg.getStartPointsOf(entryPoint)) {
				propagate(zeroValue, startPoint, zeroValue, new AllTop<V>(valueLattice.topElement()));
				pathWorklist.add(new PathEdge<N,D,M>(zeroValue, startPoint, zeroValue));
				for (D val : initialAbstraction) {
					propagate(val, startPoint, val, new AllTop<V>(valueLattice.topElement()));
					pathWorklist.add(new PathEdge<N,D,M>(zeroValue, startPoint, val));
				}
			}
		}
		forwardComputeJumpFunctionsSLRPs();		
		computeValues();
	}
	
	/**
	 * Forward-tabulates the same-level realizable paths and associated functions.
	 */
	private void forwardComputeJumpFunctionsSLRPs() {
		while(!pathWorklist.isEmpty()) {
			//pop edge
			Iterator<PathEdge<N,D,M>> iter = pathWorklist.iterator();
			PathEdge<N,D,M> edge = iter.next();
			iter.remove();
			
			if(icfg.isCallStmt(edge.getTarget())) {		
				executor.execute(new CallSiteTask(edge));
			} else if(icfg.isExitStmt(edge.getTarget())) {
				executor.execute(new ExitNodeTask(edge));
			} else {
				executor.execute(new NormalNodeTask(edge));
			}
		}
	}
	
	/**
	 * Computes the final values for edge functions.
	 */
	private void computeValues() {		
		//Phase II(i)
		for(M entryPoint: initialSeeds.keys()) {
			for(N startPoint: icfg.getStartPointsOf(entryPoint)) {
				setVal(startPoint, zeroValue, valueLattice.bottomElement());
				Pair<N, D> superGraphNode = new Pair<N,D>(startPoint, zeroValue); 
				nodeWorkList.add(superGraphNode);
			}
		}
		while(!nodeWorkList.isEmpty()) {
			Pair<N,D> nAndD = nodeWorkList.remove(0);
			N n = nAndD.getO1();
			if(icfg.isStartPoint(n)) {
				D d = nAndD.getO2();
				M p = icfg.getMethodOf(n);
				for(N c: icfg.getCallsFromWithin(p)) {					
					for(Map.Entry<D,EdgeFunction<V>> dPAndFP: jumpFn.forwardLookup(d,c).entrySet()) {
						D dPrime = dPAndFP.getKey();
						EdgeFunction<V> fPrime = dPAndFP.getValue();
						N sP = n;
						propagateValue(c,dPrime,fPrime.computeTarget(val(sP,d)));
					}
				}
			}
			if(icfg.isCallStmt(n)) {
				D d = nAndD.getO2();
				for(M q: icfg.getCalleesOfCallAt(n)) {
					FlowFunction<D> callFlowFunction = flowFunctions.getCallFlowFunction(n, q);
					for(D dPrime: callFlowFunction.computeTargets(d)) {
						EdgeFunction<V> edgeFn = edgeFunctions.getCallEdgeFunction(n, d, q, dPrime);
						for(N startPoint: icfg.getStartPointsOf(q)) {
							propagateValue(startPoint,dPrime, edgeFn.computeTarget(val(n,d)));
						}
					}
				}
			}
		}
		//Phase II(ii)
		for(N n: icfg.allNonCallStartNodes()) {
			for(N sP: icfg.getStartPointsOf(icfg.getMethodOf(n))) {
				for(Cell<D, D, EdgeFunction<V>> sourceValTargetValAndFunction : jumpFn.lookupByTarget(n)) {
					D dPrime = sourceValTargetValAndFunction.getRowKey();
					D d = sourceValTargetValAndFunction.getColumnKey();
					EdgeFunction<V> fPrime = sourceValTargetValAndFunction.getValue();
					setVal(n,d,valueLattice.join(val(n,d),fPrime.computeTarget(val(sP,dPrime))));
				}
			}
		}
	}
	
	private void propagateValue(N nHashN, D nHashD, V v) {
		V valNHash = val(nHashN, nHashD);
		V vPrime = valueLattice.join(valNHash,v);
		if(!vPrime.equals(valNHash)) {
			setVal(nHashN, nHashD, vPrime);
			nodeWorkList.add(new Pair<N,D>(nHashN,nHashD));
			System.err.println("XXX "+((SootMethod)icfg.getMethodOf(nHashN)).getName()+" "+nHashN+" "+nHashD);
		}
	}

	private V val(N nHashN, D nHashD){ 
		V l = val.get(nHashN, nHashD);
		if(l==null) return valueLattice.topElement(); //implicitly initialized to top; see line [1] of Fig. 7 in SRH96 paper
		else return l;
	}
	
	private void setVal(N nHashN, D nHashD,V l){ 
		val.put(nHashN, nHashD,l);
	}

	private EdgeFunction<V> jumpFunction(PathEdge<N, D, M> edge) {
		EdgeFunction<V> function = jumpFn.forwardLookup(edge.factAtSource(), edge.getTarget()).get(edge.factAtTarget());
		if(function==null) return allTop; //JumpFn initialized to all-top, see line [2] in SRH96 paper
		return function;
	}	
	
	private void propagate(D sourceVal, N target, D targetVal, EdgeFunction<V> f) {
		EdgeFunction<V> jumpFnE = jumpFn.reverseLookup(target, targetVal).get(sourceVal);
		if(jumpFnE==null) jumpFnE = allTop; //JumpFn is initialized to all-top (see line [2] in SRH96 paper)
		EdgeFunction<V> fPrime = jumpFnE.joinWith(f);
		if(!fPrime.equalTo(jumpFnE)) {
			jumpFn.addFunction(sourceVal, target, targetVal, fPrime);
			
			PathEdge<N,D,M> edge = new PathEdge<N,D,M>(sourceVal, target, targetVal);
			pathWorklist.add(edge);
			
			if(targetVal!=zeroValue && !(sourceVal.equals(targetVal) && icfg.isStartPoint(target))) {			
				StringBuffer result = new StringBuffer();
				result.append("<");
				result.append(icfg.getMethodOf(target));
				result.append(",");
				result.append(sourceVal);
				result.append("> -> <");
				result.append(target);
				result.append(",");
				result.append(targetVal);
				result.append("> - ");
				result.append(fPrime);
				System.err.println(result.toString());
			}
		}
	}
	

	private Map<N, Set<D>> incoming(D d1, N sP) {
		Map<N, Set<D>> map = incoming.get(sP, d1);
		if(map==null) return Collections.emptyMap();
		return map;
	}
	
	private Map<N, Set<D>> endSummary(N sP, D d3) {
		Map<N, Set<D>> map = endSummary.get(sP, d3);
		if(map==null) return Collections.emptyMap();
		return map;
	}

	private void addEndSummary(N sP, D d1, N eP, D d2) {
		Map<N, Set<D>> summaries = endSummary.get(sP, d1);
		if(summaries==null) {
			summaries = new HashMap<N, Set<D>>();
			endSummary.put(sP, d1, summaries);
		}
		Set<D> set = summaries.get(eP);
		if(set==null) {
			set = new HashSet<D>();
			summaries.put(eP,set);
		}
		set.add(d2);
	}	
	
	private void addIncoming(N sP, D d3, N n, D d2) {
		Map<N, Set<D>> summaries = incoming.get(sP, d3);
		if(summaries==null) {
			summaries = new HashMap<N, Set<D>>();
			incoming.put(sP, d3, summaries);
		}
		Set<D> set = summaries.get(n);
		if(set==null) {
			set = new HashSet<D>();
			summaries.put(n,set);
		}
		set.add(d2);
	}	
	
	/**
	 * Returns the V-type result for the given value at the given statement. 
	 */
	public V resultAt(N stmt, D value) {
		return val.get(stmt, value);
	}
	
	/**
	 * Returns the resulting environment for the given statement.
	 * The artificial zero value is automatically stripped.
	 */
	public Map<D,V> resultsAt(N stmt) {
		//filter out the artificial zero-value
		return Maps.filterKeys(val.row(stmt), new Predicate<D>() {

			public boolean apply(D val) {
				return val!=zeroValue;
			}
		});
	}
}
