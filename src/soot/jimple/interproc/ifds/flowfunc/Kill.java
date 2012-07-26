package soot.jimple.interproc.ifds.flowfunc;

import java.util.Collections;
import java.util.Set;

import soot.jimple.interproc.ifds.FlowFunction;

public class Kill<D> implements FlowFunction<D> {
	
	private final D killValue;
	
	public Kill(D killValue){
		this.killValue = killValue;
	} 

	public Set<D> computeTargets(D source) {
		if(source==killValue) {
			return Collections.emptySet();
		} else
			return Collections.singleton(source);
	}
	
}
