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
package soot.jimple.infoflow.solver;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import heros.solver.IDESolver;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedBiDiICFG;
import soot.jimple.toolkits.pointer.RWSet;
import soot.jimple.toolkits.pointer.SideEffectAnalysis;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.MHGPostDominatorsFinder;

import java.util.HashSet;
import java.util.Set;

/**
 * Interprocedural control-flow graph for the infoflow solver
 * 
 * @author Steven Arzt
 */
public class InfoflowCFG extends JimpleBasedBiDiICFG implements IInfoflowCFG {

	protected final LoadingCache<Unit,UnitContainer> unitToPostdominator =
			IDESolver.DEFAULT_CACHE_BUILDER.build( new CacheLoader<Unit,UnitContainer>() {
				@Override
				public UnitContainer load(Unit unit) throws Exception {
					SootMethod method = getMethodOf(unit);
					DirectedGraph<Unit> graph = bodyToUnitGraph.getUnchecked(method.getActiveBody());
					MHGPostDominatorsFinder<Unit> postdominatorFinder = new MHGPostDominatorsFinder<Unit>(graph);
					Unit postdom = postdominatorFinder.getImmediateDominator(unit);
					if (postdom == null)
						return new UnitContainer(method);
					else
						return new UnitContainer(postdom);
				}
			});
	
	protected final SideEffectAnalysis sideEffectAnalysis;
	
	public InfoflowCFG() {
		super();
		
		this.sideEffectAnalysis = new SideEffectAnalysis
				(Scene.v().getPointsToAnalysis(), Scene.v().getCallGraph());
	}

	/**
	 * Gets the postdominator of the given unit. If this unit is a conditional,
	 * the postdominator is the join point behind both branches of the conditional.
	 * @param u The unit for which to get the postdominator.
	 * @return The postdominator of the given unit
	 */
	public UnitContainer getPostdominatorOf(Unit u) {
		return unitToPostdominator.getUnchecked(u);
	}
	
	public Set<?> getReadVariables(SootMethod caller, Stmt inv) {
		RWSet rwSet = sideEffectAnalysis.readSet(caller, inv);
		if (rwSet == null)
			return null;
		HashSet<Object> objSet = new HashSet<Object>(rwSet.getFields());
		objSet.addAll(rwSet.getGlobals());
		return objSet;
	}
	
	public Set<?> getWriteVariables(SootMethod caller, Stmt inv) {
		RWSet rwSet = sideEffectAnalysis.writeSet(caller, inv);
		if (rwSet == null)
			return null;
		HashSet<Object> objSet = new HashSet<Object>(rwSet.getFields());
		objSet.addAll(rwSet.getGlobals());
		return objSet;
	}

}
