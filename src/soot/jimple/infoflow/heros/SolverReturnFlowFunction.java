package soot.jimple.infoflow.heros;

import java.util.Collections;
import java.util.Set;

import soot.jimple.infoflow.data.Abstraction;
import heros.FlowFunction;

public abstract class SolverReturnFlowFunction implements FlowFunction<Abstraction> {

	@Override
	public Set<Abstraction> computeTargets(Abstraction source) {
		return computeTargets(source, Collections.<Abstraction>emptySet());
	}

	public abstract Set<Abstraction> computeTargets(Abstraction source, Set<Abstraction> callerD1s);
	
}
