package soot.jimple.infoflow.taintWrappers;

import java.util.List;
import java.util.Set;

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
	
	/**
	 * Checks whether this taint wrapper wants to be queried for taint propagations on
	 * calls to methods in the given class.
	 * @param c The class containing the callees.
	 * @return True if this taint wrapper can analyze method calls into the given class,
	 * otherwise false.
	 */
	public boolean supportsTaintWrappingForClass(SootClass c);
	
	/**
	 * Checks an invocation statement for black-box taint propagation. This allows
	 * the wrapper to artificially propagate taints over method invocations without
	 * requiring the analysis to look inside the method.
	 * @param stmt The invocation statement which to check for black-box taint propagation
	 * @param taintedparam The position of the tainted argument (-1 if no argument is tainted)
	 * @param taintedBase Null if base object is not tainted or the base object or a fieldref
	 * which includes the tainted field of the base object
	 * @return The list of tainted values after the invocation statement referenced in {@link Stmt}
	 * has been executed
	 */
	public Set<Value> getTaintsForMethod(Stmt stmt, int taintedparam, Value taintedBase);

	/**
	 * Gets whether the taints produced by this taint wrapper are exclusive, i.e. there are
	 * no other taints than those produced by the wrapper. In effect, this tells the analysis
	 * not to propagate inside the callee.
	 * @param stmt The call statement to check
	 * @param taintedparam The position of the tainted argument (-1 if no argument is tainted)
	 * @param taintedBase Null if base object is not tainted or the base object or a fieldref
	 * which includes the tainted field of the base object
	 * @return True if this taint wrapper is exclusive, otherwise false. 
	 */
	public boolean isExclusive(Stmt stmt, int taintedparam, Value taintedBase);

	
	/**
	 * determines if the used TaintWrapper-Class supports taintWrapping in backward analysis
	 * @return
	 */
	public boolean supportsBackwardWrapping();
	
	/**
	 * gets a list of taints (usually the arguments or the base object) given a statement which contains of a method call 
	 * and an assignment which has a tainted left side.
	 * @param stmt the statement (leftValue is tainted)
	 * @return list of tainted values
	 */
	public List<Value> getBackwardTaintsForMethod(Stmt stmt);

}
