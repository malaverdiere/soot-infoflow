package soot.jimple.infoflow.aliasing;

import java.util.Set;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.solver.IInfoflowSolver;

/**
 * Generic interface for the different taint aliasing strategies supported by
 * FlowDroid
 * 
 * @author Steven Arzt
 */
public interface IAliasingStrategy {

	/**
	 * Computes the taints for the aliases of a given tainted variable
	 * @param d1 The context in which the variable has been tainted
	 * @param src The statement that tainted the variable
	 * @param targetValue The target value which has been tainted
	 * @param taintSet The set to which all generated alias taints shall be
	 * added
	 * @param method The method containing src
	 * @param newAbs The newly generated abstraction for the variable taint
	 */
	public void computeAliasTaints
			(final Abstraction d1, final Stmt src,
			final Value targetValue, Set<Abstraction> taintSet,
			SootMethod method, Abstraction newAbs);
	
	/**
	 * Gets whether this aliasing strategy is interactive, i.e. computes aliases
	 * on demand.
	 * @return True if this is an on-demand aliasing strategy, otherwise false
	 */
	public boolean isInteractive();
	
	/**
	 * Gets whether the two given access path may alias
	 * @param ap1 The first access path
	 * @param ap2 The second access path
	 * @return True if the two access paths can potentially point ot the same
	 * runtime object, otherwise false
	 */
	public boolean mayAlias(AccessPath ap1, AccessPath ap2);
	
	/**
	 * Sets the forward solver. Implementors can use this reference to inject
	 * edges for taint aliases at right position.
	 * @param fSolver The forward solver performing the taint propagation
	 */
	public void setForwardSolver(IInfoflowSolver fSolver);

}
