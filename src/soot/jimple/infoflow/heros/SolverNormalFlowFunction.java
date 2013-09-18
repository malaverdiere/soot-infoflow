package soot.jimple.infoflow.heros;

import heros.FlowFunction;

import java.util.Set;

import soot.jimple.infoflow.data.Abstraction;

public abstract class SolverNormalFlowFunction implements FlowFunction<Abstraction> {

	@Override
	public Set<Abstraction> computeTargets(Abstraction source) {
		return computeTargets(null, source);
	}

	public abstract Set<Abstraction> computeTargets(Abstraction d1, Abstraction d2);
	
}
