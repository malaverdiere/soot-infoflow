package soot.jimple.infoflow.heros;

import java.util.Collections;
import java.util.Set;

import soot.jimple.infoflow.data.Abstraction;
import heros.FlowFunction;

/**
 * A special implementation of the return flow function that allows access to
 * the facts associated with the start points of the callees (i.e. the contexts
 * to which we return).
 *  
 * @author Steven Arzt
 */
public abstract class SolverReturnFlowFunction implements FlowFunction<Abstraction> {

	@Override
	public Set<Abstraction> computeTargets(Abstraction source) {
		return computeTargets(source, Collections.<Abstraction>emptySet());
	}

	/**
	 * Computes the abstractions at the return site.
	 * @param source The abstraction at the exit node
	 * @param callerD1s The abstractions at the start nodes of all methods to
	 * which we return (i.e. the contexts to which this flow function will be
	 * applied).
	 * @return The set of abstractions at the return site.
	 */
	public abstract Set<Abstraction> computeTargets(Abstraction source, Set<Abstraction> callerD1s);
	
}
