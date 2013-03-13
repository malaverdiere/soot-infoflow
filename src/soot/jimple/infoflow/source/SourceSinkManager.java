package soot.jimple.infoflow.source;

import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public interface SourceSinkManager {

	public abstract boolean isSource(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg);
	public abstract boolean isSink(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg);

}
