package soot.jimple.infoflow.taintWrappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
	public Set<Value> getTaintsForMethod(Stmt stmt, int taintedparam, Value taintedBase) {
		// If the base object is tainted, the return value is always tainted
		if (taintedBase != null)
			if (stmt instanceof JAssignStmt)
				return Collections.singleton(((JAssignStmt)stmt).getLeftOp());
		
		// If one of the parameters is tainted, the return value is tainted, too
		if (taintedparam >= 0)
			if (stmt instanceof JAssignStmt)
				return Collections.singleton(((JAssignStmt)stmt).getLeftOp());
		
		return Collections.emptySet();
	}

	@Override
	public boolean isExclusive(Stmt stmt, int taintedparam, Value taintedBase) {
		return (taintedparam >= 0 || taintedBase != null);
	}

	
	@Override
	public boolean supportsBackwardWrapping() {
		//TODO: implement this
		return false;
	}

	@Override
	public List<Value> getBackwardTaintsForMethod(Stmt stmt) {
		//TODO: implement this
		return null;
	}


}
