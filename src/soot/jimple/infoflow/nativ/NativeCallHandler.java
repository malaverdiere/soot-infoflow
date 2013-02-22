package soot.jimple.infoflow.nativ;

import java.util.List;
import java.util.Set;

import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.infoflow.AbstractInfoflowProblem.PathTrackingMethod;
import soot.jimple.infoflow.data.Abstraction;

public abstract class NativeCallHandler {

	public abstract void setPathTracking(PathTrackingMethod method);
	

	public abstract Set<Abstraction> getTaintedValues(Stmt call, Abstraction source, List<Value> params);

	public abstract Set<Abstraction> getTaintedValuesForBackwardAnalysis(Stmt call, Abstraction source, List<Value> params);
	
}
