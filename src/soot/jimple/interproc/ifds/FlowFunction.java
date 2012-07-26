package soot.jimple.interproc.ifds;

import java.util.Set;

/**
 * A flow function computes which of the finitely many D-type values are reachable
 * from the current source values. Typically there will be one such function
 * associated with every possible control flow. 
 *
 * @param <D> The type of data-flow facts to be computed by the tabulation problem.
 */
public interface FlowFunction<D> {

	/**
	 * Returns the target values reachable from the source.
	 */
	Set<D> computeTargets(D source);
}
