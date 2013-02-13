package soot.jimple.infoflow.taintWrappers;

import java.util.Collections;
import java.util.List;

import soot.SootClass;
import soot.Value;
import soot.jimple.Stmt;
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
	public boolean supportsTaintWrappingForClass(SootClass c) {
		return true;
	}

	@Override
	public List<Value> getTaintsForMethod(Stmt stmt, int taintedparam, Value taintedBase) {
		// If the base object is tainted, the return value is always tainted
		if (taintedBase != null)
			if (stmt instanceof JAssignStmt)
				return Collections.singletonList(((JAssignStmt)stmt).getLeftOp());
		
		// If one of the parameters is tainted, the return value is tainted, too
		if (taintedparam >= 0)
			if (stmt instanceof JAssignStmt)
				return Collections.singletonList(((JAssignStmt)stmt).getLeftOp());
		
		return Collections.emptyList();
	}

}
