package soot.jimple.infoflow.source;

import soot.SootMethod;

public interface SourceSinkManager {

	public abstract boolean isSourceMethod(SootMethod sMethod);
	public abstract boolean isSinkMethod(SootMethod sMethod);

}
