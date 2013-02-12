package soot.jimple.infoflow.util;

import java.util.List;

import soot.SootClass;
import soot.Value;
import soot.jimple.Stmt;

/**
 * This interface declares methods to define classes and methods which should not be analyzed directly.
 * Instead the outcome of the analysis is summarized (which improves performance and helps if the sources are not available)
 * @author Christian
 *
 */
public interface ITaintPropagationWrapper {
	
	public boolean supportsTaintWrappingForClass(SootClass c);
	
	public boolean supportsBackwardWrapping();
	
	/**
	 * 
	 * @param stmt the Stmt
	 * @param taintedparam position of the tainted argument (-1 if no argument is tainted)
	 * @param taintedBase null if base object is not tainted or the base object or a fieldref which includes the tainted field of the base object
	 * @return list of tainted values
	 */
	public List<Value> getTaintsForMethod(Stmt stmt, int taintedparam, Value taintedBase);
	
	/**
	 * 
	 * @param stmt the Stmt (leftValue is tainted)
	 * @return
	 */
	public List<Value> getBackwardTaintsForMethod(Stmt stmt);

}
