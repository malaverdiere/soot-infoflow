package soot.jimple.interproc.ifds.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.interproc.ifds.InterproceduralCFG;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.EdgePredicate;
import soot.jimple.toolkits.callgraph.Filter;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * Default implementation for the {@link InterproceduralCFG} interface.
 * Includes all statements reachable from {@link Scene#getEntryPoints()} through
 * explicit call statements or through calls to {@link Thread#start()}.
 */
public class JimpleBasedInterproceduralCFG implements InterproceduralCFG<Unit,SootMethod> {
	
	protected final CallGraph cg;
	
	protected final Map<Unit,Body> unitToOwner = new HashMap<Unit,Body>();
	
	protected final Map<Body,DirectedGraph<Unit>> bodyToUnitGraph = new HashMap<Body,DirectedGraph<Unit>>();
	
	//retains only callers that are explicit call sites or Thread.start()
	protected final Filter EDGE_FILTER = new Filter(new EdgePredicate() {
		public boolean want(Edge e) {				
			return e.kind().isExplicit() || e.kind().isThread();
		}
	});

	public JimpleBasedInterproceduralCFG() {
		cg = Scene.v().getCallGraph();
		
		List<MethodOrMethodContext> eps = new ArrayList<MethodOrMethodContext>();
		eps.addAll(Scene.v().getEntryPoints());
		ReachableMethods reachableMethods = new ReachableMethods(cg, eps.iterator());
		reachableMethods.update();
		
		for(Iterator<MethodOrMethodContext> iter = reachableMethods.listener(); iter.hasNext(); ) {
			SootMethod m = iter.next().method();
			if(m.hasActiveBody()) {
				Body b = m.getActiveBody();
				PatchingChain<Unit> units = b.getUnits();
				for (Unit unit : units) {
					unitToOwner.put(unit, b);
				}
			}
		}
	}

	public SootMethod getMethodOf(Unit u) {
		return unitToOwner.get(u).getMethod();
	}

	public List<Unit> getSuccsOf(Unit u) {
		Body body = unitToOwner.get(u);
		DirectedGraph<Unit> unitGraph = getOrCreateUnitGraph(body);
		return unitGraph.getSuccsOf(u);
	}

	private DirectedGraph<Unit> getOrCreateUnitGraph(Body body) {
		DirectedGraph<Unit> unitGraph = bodyToUnitGraph.get(body);
		if(unitGraph==null) {
			unitGraph = makeGraph(body); 
			bodyToUnitGraph.put(body, unitGraph);
		}
		return unitGraph;
	}

	protected DirectedGraph<Unit> makeGraph(Body body) {
		return new ExceptionalUnitGraph(body);
	}

	public Set<SootMethod> getCalleesOfCallAt(Unit u) {
		//TODO implement soft cache
		Set<SootMethod> res = new HashSet<SootMethod>();

		//only retain callers that are explicit call sites or Thread.start()
		Iterator<Edge> edgeIter = EDGE_FILTER.wrap(cg.edgesOutOf(u));
		
		while(edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			SootMethod m = edge.getTgt().method();
			if(m.hasActiveBody())
			res.add(m);
		}
		return res; 
	}

	public List<Unit> getReturnSitesOfCallAt(Unit u) {
		return getSuccsOf(u);
	}

	public boolean isCallStmt(Unit u) {
		return EDGE_FILTER.wrap(cg.edgesOutOf(u)).hasNext();
	}

	public boolean isExitStmt(Unit u) {
		Body body = unitToOwner.get(u);
		DirectedGraph<Unit> unitGraph = getOrCreateUnitGraph(body);
		return unitGraph.getTails().contains(u);
	}

	public Set<Unit> getCallersOf(SootMethod m) {
		//TODO implement soft cache
		Set<Unit> res = new HashSet<Unit>();
		
		//only retain callers that are explicit call sites or Thread.start()
		Iterator<Edge> edgeIter = EDGE_FILTER.wrap(cg.edgesInto(m));
		
		while(edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			res.add(edge.srcUnit());			
		}
		return res;
	}
	
	public Set<Unit> getCallsFromWithin(SootMethod m) {
		//TODO implement soft cache
		Set<Unit> res = new HashSet<Unit>();
		
		//only retain callers that are explicit call sites or Thread.start()
		Iterator<Edge> edgeIter = EDGE_FILTER.wrap(cg.edgesOutOf(m));
		
		while(edgeIter.hasNext()) {
			Edge edge = edgeIter.next();
			res.add(edge.srcUnit());			
		}
		return res;
	}

	public Set<Unit> getStartPointsOf(SootMethod m) {
		if(m.hasActiveBody()) {
			Body body = m.getActiveBody();
			DirectedGraph<Unit> unitGraph = getOrCreateUnitGraph(body);
			return new HashSet<Unit>(unitGraph.getHeads());
		}
		return null;
	}

	public boolean isStartPoint(Unit u) {
		return unitToOwner.get(u).getUnits().getFirst()==u;
	}

	public Set<Unit> allNonCallStartNodes() {
		Set<Unit> res = new HashSet<Unit>(unitToOwner.keySet());
		for (Iterator<Unit> iter = res.iterator(); iter.hasNext();) {
			Unit u = iter.next();
			if(isStartPoint(u) || isCallStmt(u)) iter.remove();
		}
		return res;
	}

}
