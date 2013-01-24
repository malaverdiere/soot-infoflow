package soot.jimple.infoflow.util;

import soot.Local;
import soot.SootClass;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;

public class ListExampleWrapper implements ITaintPropagationWrapper {

	@Override
	public boolean supportsTaintWrappingForClass(SootClass c) {
		if(c.implementsInterface("java.util.List"))
			return true;

		return false;
	}

	@Override
	public Value getTaintForMethod(Stmt stmt, int taintedparam, Value taintedBase) {
		// method add + added element is tainted -> whole list is tainted
		if(stmt.getInvokeExpr().getMethod().getSubSignature().equals("boolean add(java.lang.Object)") && taintedparam == 0){
			return ((InstanceInvokeExpr) stmt.getInvokeExprBox().getValue()).getBase();
		}
		// method get + whole list is tainted -> returned element is tainted
		if(stmt.getInvokeExpr().getMethod().getSubSignature().equals("java.lang.Object get(int)") && taintedBase instanceof Local){
			if(stmt instanceof JAssignStmt){
				return ((JAssignStmt)stmt).getLeftOp();
			}
		}
		return null;
	}

}
