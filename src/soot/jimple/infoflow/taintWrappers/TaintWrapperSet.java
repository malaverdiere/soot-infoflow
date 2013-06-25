package soot.jimple.infoflow.taintWrappers;

import java.util.HashSet;
import java.util.Set;

import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;

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
	public Set<AccessPath> getTaintsForMethod(Stmt stmt, AccessPath taintedPath) {
		Set<AccessPath> resList = new HashSet<AccessPath>();
		for (ITaintPropagationWrapper w : this.wrappers)
			resList.addAll(w.getTaintsForMethod(stmt, taintedPath));
		return new HashSet<AccessPath>(resList);
	}

	@Override
	public boolean isExclusive(Stmt stmt, AccessPath taintedPath) {
		for (ITaintPropagationWrapper w : this.wrappers)
			if (w.isExclusive(stmt, taintedPath))
				return true;
		return false;
	}
}
