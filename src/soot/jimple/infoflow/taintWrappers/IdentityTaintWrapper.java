package soot.jimple.infoflow.taintWrappers;

import java.util.Collections;
import java.util.Set;

import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.internal.JAssignStmt;

/**
 * Taints the return value of a method call if one of the parameter values
 * or the base object is tainted.
 * 
 * @author Steven Arzt
 *
 */
public class IdentityTaintWrapper implements ITaintPropagationWrapper {

	@Override
	public Set<AccessPath> getTaintsForMethod(Stmt stmt, AccessPath taintedPath) {
		assert stmt.containsInvokeExpr();
		if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
			InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
			
			// If the base object is tainted, the return value is always tainted
			if (taintedPath.getPlainValue().equals(iiExpr.getBase()))
				if (stmt instanceof JAssignStmt)
					return Collections.singleton(new AccessPath(((JAssignStmt)stmt).getLeftOp()));
		}
			
		// If one of the parameters is tainted, the return value is tainted, too
		for (Value param : stmt.getInvokeExpr().getArgs())
			if (taintedPath.getPlainValue().equals(param))
				if (stmt instanceof JAssignStmt)
					return Collections.singleton(new AccessPath(((JAssignStmt)stmt).getLeftOp()));
		
		return Collections.emptySet();
	}

	@Override
	public boolean isExclusive(Stmt stmt, AccessPath taintedPath) {
		assert stmt.containsInvokeExpr();
		
		// We are exclusive if the base object is tainted
		if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
			InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
			if (taintedPath.getPlainValue().equals(iiExpr.getBase()))
				return true;
		}
		
		// If one parameter is tainted, we are exclusive as well
		for (Value param : stmt.getInvokeExpr().getArgs())
			if (taintedPath.getPlainValue().equals(param))
				return true;
		
		return false;
	}

}