package soot.jimple.interproc.ifds.flowfunc;

import java.util.Collections;
import java.util.Set;

import soot.jimple.interproc.ifds.FlowFunction;

public class KillAll<D> implements FlowFunction<D> {
	
	@SuppressWarnings("rawtypes")
	private final static KillAll instance = new KillAll();
	
	private KillAll(){} //use v() instead

	public Set<D> computeTargets(D source) {
		return Collections.emptySet();
	}
	
	@SuppressWarnings("unchecked")
	public static <D> KillAll<D> v() {
		return instance;
	}

}
