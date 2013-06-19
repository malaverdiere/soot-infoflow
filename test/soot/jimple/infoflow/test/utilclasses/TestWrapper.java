package soot.jimple.infoflow.test.utilclasses;

import java.util.HashSet;
import java.util.Set;

import soot.SootClass;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;

public class TestWrapper implements ITaintPropagationWrapper {

	@Override
	public boolean supportsTaintWrappingForClass(SootClass c) {
		if(c.toString().contains("ClassWith")){
			return true;
		}
		return false;
	}

	@Override
	public Set<AccessPath> getTaintsForMethod(Stmt stmt, int taintedparam, Value taintedBase) {
		return new HashSet<AccessPath>();
	}

	@Override
	public boolean isExclusive(Stmt stmt, int taintedparam, Value taintedBase) {
		return true;
	}

}
