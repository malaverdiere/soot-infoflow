package soot.jimple.infoflow.taintWrappers;

import java.util.HashSet;
import java.util.Set;

import soot.Local;
import soot.SootClass;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;

/**
 * Internal taint wrapper for the use in some test cases
 */
public class ListExampleWrapper implements ITaintPropagationWrapper {

	@Override
	public boolean supportsTaintWrappingForClass(SootClass c) {
		if(c.implementsInterface("java.util.List"))
			return true;

		return false;
	}

	@Override
	public Set<Value> getTaintsForMethod(Stmt stmt, int taintedparam, Value taintedBase) {
		// method add + added element is tainted -> whole list is tainted
		if(stmt.getInvokeExpr().getMethod().getSubSignature().equals("boolean add(java.lang.Object)") && taintedparam == 0){
			Set<Value> taints = new HashSet<Value>();
			taints.add(((InstanceInvokeExpr) stmt.getInvokeExprBox().getValue()).getBase());
			return taints;
		}
		// method get + whole list is tainted -> returned element is tainted
		if(stmt.getInvokeExpr().getMethod().getSubSignature().equals("java.lang.Object get(int)") && taintedBase instanceof Local){
			if(stmt instanceof JAssignStmt){
				Set<Value> taints = new HashSet<Value>();
				taints.add(((JAssignStmt)stmt).getLeftOp());
				return taints;
			}
		}
		return null;
	}

	@Override
	public boolean isExclusive(Stmt stmt, int taintedparam, Value taintedBase) {
		return false;
	}
}
