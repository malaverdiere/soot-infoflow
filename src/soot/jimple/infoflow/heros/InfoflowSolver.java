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

import java.util.Set;

import heros.FlowFunction;
import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import heros.edgefunc.EdgeIdentity;
import heros.solver.CountingThreadPoolExecutor;
import heros.solver.IFDSSolver;
import heros.solver.PathEdge;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
/**
 * We are subclassing the JimpleIFDSSolver because we need the same executor for both the forward and the backward analysis
 * Also we need to be able to insert edges containing new taint information
 * 
 */
public class InfoflowSolver extends JimpleIFDSSolver<Abstraction, InterproceduralCFG<Unit, SootMethod>> {

	public InfoflowSolver(IFDSTabulationProblem<Unit, Abstraction, SootMethod, InterproceduralCFG<Unit, SootMethod>> problem, boolean dumpResults, CountingThreadPoolExecutor executor) {
		super(problem, dumpResults);
		this.executor = executor;
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
			jumpFn.addFunction(edge.factAtSource(), edge.getTarget(), edge.factAtTarget(),
					EdgeIdentity.<IFDSSolver.BinaryDomain>v());
			scheduleEdgeProcessing(edge);
			return true;
		}
		return false;
	}
	
	@Override
	protected Set<Abstraction> computeReturnFlowFunction
			(FlowFunction<Abstraction> retFunction, Abstraction d2, Set<Abstraction> callerSideD1s) {
		if (retFunction instanceof SolverReturnFlowFunction)
			return ((SolverReturnFlowFunction) retFunction).computeTargets(d2, callerSideD1s);
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
	
	protected Set<Abstraction> computeCallToReturnFlowFunction
			(FlowFunction<Abstraction> flowFunction, Abstraction d1, Abstraction d2) {
		if (flowFunction instanceof SolverCallToReturnFlowFunction)
			return ((SolverCallToReturnFlowFunction) flowFunction).computeTargets(d1, d2);
		else
			return flowFunction.computeTargets(d2);		
	}

}
