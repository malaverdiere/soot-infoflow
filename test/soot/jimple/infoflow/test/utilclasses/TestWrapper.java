package soot.jimple.infoflow.test.utilclasses;

import java.util.HashSet;
import java.util.Set;

import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;

public class TestWrapper implements ITaintPropagationWrapper {

	@Override
	public Set<AccessPath> getTaintsForMethod(Stmt stmt, AccessPath taintedPath) {
		return new HashSet<AccessPath>();
	}

	@Override
	public boolean isExclusive(Stmt stmt, AccessPath taintedPath) {
		return false;
	}

}
