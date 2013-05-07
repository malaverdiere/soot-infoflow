package soot.jimple.infoflow.source;

import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public interface SourceSinkManager {

	/**
	 * determines if a method called by the Stmt is a source method or not
	 * @param sCallSite a Stmt which should include an invokeExrp calling a method
	 * @param cfg the interprocedural controlflow graph
	 * @return true if source method is called
	 */
	public abstract boolean isSource(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg);
	/**
	 * determines if a method called by the Stmt is a sink method or not
	 * @param sCallSite a Stmt which should include an invokeExrp calling a method
	 * @param cfg the interprocedural controlflow graph
	 * @return true if sink method is called
	 */
	public abstract boolean isSink(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg);

}
