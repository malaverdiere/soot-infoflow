/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow.heros;

import heros.EdgeFunction;
import heros.FlowFunction;
import heros.InterproceduralCFG;
import heros.edgefunc.EdgeIdentity;
import heros.solver.CountingThreadPoolExecutor;
import heros.solver.IFDSSolver;
import heros.solver.PathEdge;
import heros.solver.PathTrackingIFDSSolver;

import java.util.HashSet;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.problems.AbstractInfoflowProblem;
/**
 * We are subclassing the JimpleIFDSSolver because we need the same executor for both the forward and the backward analysis
 * Also we need to be able to insert edges containing new taint information
 * 
 */
public class InfoflowSolver extends PathTrackingIFDSSolver<Unit, Abstraction, SootMethod, InterproceduralCFG<Unit, SootMethod>> {

	public InfoflowSolver(AbstractInfoflowProblem problem, CountingThreadPoolExecutor executor) {
		super(problem);
		this.executor = executor;
		problem.setSolver(this);		
	}
	
	@Override
	protected CountingThreadPoolExecutor getExecutor() {
		return executor;
	}

	public boolean processEdge(PathEdge<Unit, Abstraction> edge){
		// We are generating a fact out of thin air here. If we have an
		// edge <d1,n,d2>, there need not necessarily be a jump function
		// to <n,d2>.
		if (!jumpFn.forwardLookup(edge.factAtSource(), edge.getTarget()).containsKey(edge.factAtTarget())) {
			propagate(edge.factAtSource(), edge.getTarget(), edge.factAtTarget(),
					EdgeIdentity.<IFDSSolver.BinaryDomain>v(), null, false);
			return true;
		}
		return false;
	}
	
	public void injectContext(InfoflowSolver otherSolver, SootMethod callee, Abstraction d3, Unit callSite, Abstraction d2) {
		synchronized (incoming) {
			for (Unit sP : icfg.getStartPointsOf(callee))
				addIncoming(sP, d3, callSite, d2);
		}
		
		// First, get a list of the other solver's jump functions.
		// Then release the lock on otherSolver.jumpFn before doing
		// anything that locks our own jumpFn.
		final Set<Abstraction> otherAbstractions;
		synchronized (otherSolver.jumpFn) {
			otherAbstractions = new HashSet<Abstraction>
					(otherSolver.jumpFn.reverseLookup(callSite, d2).keySet());
		}
		for (Abstraction d1: otherAbstractions)
			if (!d1.getAccessPath().isEmpty() && !d1.getAccessPath().isStaticFieldRef())
				processEdge(new PathEdge<Unit, Abstraction>(d1, callSite, d2));
	}
	
	@Override
	protected Set<Abstraction> computeReturnFlowFunction
			(FlowFunction<Abstraction> retFunction, Abstraction d2, Unit callSite, Set<Abstraction> callerSideDs) {
		if (retFunction instanceof SolverReturnFlowFunction) {
			// Get the d1s at the start points of the caller
			Set<Abstraction> d1s = new HashSet<Abstraction>(callerSideDs.size() * 5);
			for (Abstraction d4 : callerSideDs)
				if (d4 == zeroValue)
					d1s.add(d4);
				else
					synchronized (jumpFn) {
						d1s.addAll(jumpFn.reverseLookup(callSite, d4).keySet());
					}
			
			return ((SolverReturnFlowFunction) retFunction).computeTargets(d2, d1s);
		}
		else
			return retFunction.computeTargets(d2);
	}

	@Override
	protected Set<Abstraction> computeNormalFlowFunction
			(FlowFunction<Abstraction> flowFunction, Abstraction d1, Abstraction d2) {
		if (flowFunction instanceof SolverNormalFlowFunction)
			return ((SolverNormalFlowFunction) flowFunction).computeTargets(d1, d2);
		else
			return flowFunction.computeTargets(d2);
	}

	@Override
	protected Set<Abstraction> computeCallToReturnFlowFunction
			(FlowFunction<Abstraction> flowFunction, Abstraction d1, Abstraction d2) {
		if (flowFunction instanceof SolverCallToReturnFlowFunction)
			return ((SolverCallToReturnFlowFunction) flowFunction).computeTargets(d1, d2);
		else
			return flowFunction.computeTargets(d2);		
	}

	@Override
	protected Set<Abstraction> computeCallFlowFunction
			(FlowFunction<Abstraction> flowFunction, Abstraction d1, Abstraction d2) {
		if (flowFunction instanceof SolverCallFlowFunction)
			return ((SolverCallFlowFunction) flowFunction).computeTargets(d1, d2);
		else
			return flowFunction.computeTargets(d2);		
	}

	@Override
	protected void propagate(Abstraction sourceVal, Unit target, Abstraction targetVal, EdgeFunction<BinaryDomain> f,
			/* deliberately exposed to clients */ Unit relatedCallSite,
			/* deliberately exposed to clients */ boolean isUnbalancedReturn) {	
		// Check whether we already have an abstraction that entails the new one.
		// In such a case, we can simply ignore the new abstraction.
		boolean noProp = false;
		/*
		for (Abstraction abs : new HashSet<Abstraction>(jumpFn.forwardLookup(sourceVal, target).keySet()))
			if (abs != targetVal) {
				if (abs.entails(targetVal)) {
					noProp = true;
					break;
				}
				if (targetVal.entails(abs)) {
					jumpFn.removeFunction(sourceVal, target, abs);
				}
			}
		*/
		if (!noProp)
			super.propagate(sourceVal, target, targetVal, f, relatedCallSite, isUnbalancedReturn);
	}

	/**
	 * Cleans up some unused memory. Results will still be available afterwards,
	 * but no intermediate computation values.
	 */
	public void cleanup() {
		this.jumpFn.clear();
		this.incoming.clear();
		this.endSummary.clear();
		this.val.clear();
		this.cache.clear();
	}
	
}
