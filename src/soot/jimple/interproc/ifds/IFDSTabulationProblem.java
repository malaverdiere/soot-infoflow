package soot.jimple.interproc.ifds;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.interproc.ifds.solver.IFDSSolver;
import soot.jimple.interproc.ifds.template.JimpleBasedInterproceduralCFG;

import com.google.common.collect.Multimap;

/**
 * A tabulation problem for solving in an {@link IFDSSolver} as described
 * by the Reps, Horwitz, Sagiv 1995 (RHS95) paper.
 *
 * @param <N> The type of nodes in the interprocedural control-flow graph. Typically {@link Unit}.
 * @param <D> The type of data-flow facts to be computed by the tabulation problem.
 * @param <M> The type of objects used to represent methods. Typically {@link SootMethod}.
 */
public interface IFDSTabulationProblem<N,D,M> {

	/**
	 * Returns a set of flow functions. Those functions are used to compute data-flow facts
	 * along the various kinds of control flows.
	 */
	FlowFunctions<N,D,M> flowFunctions();
	
	/**
	 * Returns the interprocedural control-flow graph which this problem is computed over.
	 * Typically this will be a {@link JimpleBasedInterproceduralCFG}.
	 */
	InterproceduralCFG<N,M> interproceduralCFG();
	
	/**
	 * Returns initial seeds to be used for the analysis. The multi-map maps
	 * methods (entry points) to the initial data-flow facts that hold at
	 * entry points to those methods.
	 */
	Multimap<M,D> initialSeeds();
	
	/**
	 * This must be a data-flow fact of type {@link D}, but must <i>not</i>
	 * be part of the domain of data-flow facts. Typically this will be a
	 * singleton object of type {@link D} that is used for nothing else.
	 * It must holds that this object does not equals any object 
	 * within the domain.
	 */
	D zeroValue();
}
