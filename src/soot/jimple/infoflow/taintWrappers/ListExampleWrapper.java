package soot.jimple.infoflow.taintWrappers;

import java.util.Collections;
import java.util.Set;

import soot.SootClass;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;
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
	public Set<AccessPath> getTaintsForMethod(Stmt stmt, AccessPath taintedPath) {
		// method add + added element is tainted -> whole list is tainted
		if(stmt.getInvokeExpr().getMethod().getSubSignature().equals("boolean add(java.lang.Object)"))
			if (taintedPath.getPlainValue().equals(stmt.getInvokeExpr().getArg(0)))
				return Collections.singleton(new AccessPath(((InstanceInvokeExpr) stmt.getInvokeExprBox().getValue()).getBase()));

		// method get + whole list is tainted -> returned element is tainted
		if(stmt.getInvokeExpr().getMethod().getSubSignature().equals("java.lang.Object get(int)"))
			if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
				InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
				if (taintedPath.getPlainValue().equals(iiExpr.getBase()))
					if(stmt instanceof JAssignStmt)
						return Collections.singleton(new AccessPath(((JAssignStmt)stmt).getLeftOp()));
			}

		return Collections.emptySet();
	}

	@Override
	public boolean isExclusive(Stmt stmt, AccessPath taintedPath) {
		return false;
	}
}
