package soot.jimple.infoflow.source;

import soot.jimple.Stmt;

public interface SourceSinkManager {

	public abstract boolean isSource(Stmt sCallSite);
	public abstract boolean isSink(Stmt sCallSite);

}
