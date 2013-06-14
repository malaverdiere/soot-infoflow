package soot.jimple.infoflow.taintWrappers;

import java.util.HashSet;
import java.util.Set;

import soot.SootClass;
import soot.Value;
import soot.jimple.Stmt;

/**
 * Set of taint wrappers. It supports taint wrapping for a class if at least one
 * of the contained wrappers supports it. The resulting taints are the union of
 * all taints produced by the contained wrappers.
 * 
 * @author Steven Arzt
 */
public class TaintWrapperSet implements ITaintPropagationWrapper {

	private Set<ITaintPropagationWrapper> wrappers = new HashSet<ITaintPropagationWrapper>();
	
	/**
	 * Adds the given wrapper to the chain of wrappers.
	 * @param wrapper The wrapper to add to the chain.
	 */
	public void addWrapper(ITaintPropagationWrapper wrapper) {
		this.wrappers.add(wrapper);
	}
	
	@Override
	public boolean supportsTaintWrappingForClass(SootClass c) {
		for (ITaintPropagationWrapper w : this.wrappers)
			if (w.supportsTaintWrappingForClass(c))
				return true;
		return false;
	}

	@Override
	public Set<Value> getTaintsForMethod(Stmt stmt, int taintedparam,
			Value taintedBase) {
		Set<Value> resList = new HashSet<Value>();
		for (ITaintPropagationWrapper w : this.wrappers)
			resList.addAll(w.getTaintsForMethod(stmt, taintedparam, taintedBase));
		return new HashSet<Value>(resList);
	}

	@Override
	public boolean isExclusive(Stmt stmt, int taintedparam, Value taintedBase) {
		for (ITaintPropagationWrapper w : this.wrappers)
			if (w.isExclusive(stmt, taintedparam, taintedBase))
				return true;
		return false;
	}
}
