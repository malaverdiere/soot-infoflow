package soot.jimple.infoflow.nativ;

import java.util.List;
import java.util.Set;

import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.infoflow.AbstractInfoflowProblem.PathTrackingMethod;
import soot.jimple.infoflow.data.Abstraction;

/**
 * The NativeCallHandler defines the taint propagation behavior for native code,
 * as we cannot analyze these methods
 */
public abstract class NativeCallHandler {

	/**
	 * sets the type/method of path tracking, if necessary (default is no tracking)
	 * @param method
	 */
	public abstract void setPathTracking(PathTrackingMethod method);
	
	/**
	 * returns the set of tainted values for a given call to native code, a given tainted value and the list of passed arguments
	 * @param call the statement which contains the call to the native code
	 * @param source the incoming taint value
	 * @param params list of arguments
	 * @return the resulting set of taints
	 */
	public abstract Set<Abstraction> getTaintedValues(Stmt call, Abstraction source, List<Value> params);

}
