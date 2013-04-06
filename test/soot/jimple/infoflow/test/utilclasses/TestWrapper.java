package soot.jimple.infoflow.test.utilclasses;

import java.util.LinkedList;
import java.util.List;

import soot.SootClass;
import soot.Value;
import soot.jimple.Stmt;
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
	public List<Value> getTaintsForMethod(Stmt stmt, int taintedparam, Value taintedBase) {
		return new LinkedList<Value>();
	}

	@Override
	public boolean isExclusive(Stmt stmt, int taintedparam, Value taintedBase) {
		return true;
	}

	@Override
	public boolean supportsBackwardWrapping() {
		return true;
	}

	@Override
	public List<Value> getBackwardTaintsForMethod(Stmt stmt) {
		return new LinkedList<Value>();
	}

}
