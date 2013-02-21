package soot.jimple.infoflow.source;

import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * Abstracts from the very generic statement-based SourceSinkManager so that users
 * can conveniently work on the called methods instead of having to analyze the
 * call statement every time
 * 
 * @author Steven Arzt
 *
 */
public abstract class MethodBasedSourceSinkManager implements SourceSinkManager {

	public abstract boolean isSourceMethod(SootMethod method);
	public abstract boolean isSinkMethod(SootMethod method);
	
	@Override
	public boolean isSource(Stmt sCallSite) {
		return sCallSite.containsInvokeExpr()
				&& isSourceMethod(sCallSite.getInvokeExpr().getMethod());
				 
	}

	@Override
	public boolean isSink(Stmt sCallSite) {
		return sCallSite.containsInvokeExpr()
				&& isSinkMethod(sCallSite.getInvokeExpr().getMethod());
	}

}
