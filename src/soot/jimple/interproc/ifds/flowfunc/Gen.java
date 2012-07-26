package soot.jimple.interproc.ifds.flowfunc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.jimple.interproc.ifds.FlowFunction;

public class Gen<D> implements FlowFunction<D> {
	
	private final D genValue;
	
	public Gen(D genValue){
		this.genValue = genValue;
	} 

	public Set<D> computeTargets(D source) {
		if(source==null) {
			HashSet<D> res = new HashSet<D>();
			res.add(source);
			res.add(genValue);
			return res;
		} else
			return Collections.singleton(source);
	}
	
}
