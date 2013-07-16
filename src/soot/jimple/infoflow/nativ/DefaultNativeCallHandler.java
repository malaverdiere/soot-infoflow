package soot.jimple.infoflow.nativ;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Value;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.AbstractInfoflowProblem.PathTrackingMethod;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionWithPath;
import soot.jimple.infoflow.util.DataTypeHandler;

public class DefaultNativeCallHandler extends NativeCallHandler {
	
	PathTrackingMethod pathTracking = PathTrackingMethod.NoTracking;
	
	@Override
	public void setPathTracking(PathTrackingMethod method) {
		this.pathTracking = method;
	}

	@Override
	public Set<Abstraction> getTaintedValues(Stmt call, Abstraction source, List<Value> params){
		HashSet<Abstraction> set = new HashSet<Abstraction>();
		
		//check some evaluated methods:
		
		//arraycopy:
		//arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        //Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array.
		if(call.getInvokeExpr().getMethod().toString().contains("arraycopy")){
			if(params.get(0).equals(source.getAccessPath().getPlainValue())){
				Abstraction abs = source.deriveNewAbstraction(params.get(2), call);
				if (pathTracking == PathTrackingMethod.ForwardTracking)
					((AbstractionWithPath) abs).addPathElement(call);
				set.add(abs);
			}
		}else{
			//generic case: add taint to all non-primitive datatypes:
			for (int i = 0; i < params.size(); i++) {
				Value argValue = params.get(i);
				if (DataTypeHandler.isFieldRefOrArrayRef(argValue) && !(argValue instanceof Constant)) {
					Abstraction abs = source.deriveNewAbstraction(argValue, call);
					if (pathTracking == PathTrackingMethod.ForwardTracking)
						((AbstractionWithPath) abs).addPathElement(call);
				}
			}	
		}
		//add the  returnvalue:
		if(call instanceof DefinitionStmt){
			DefinitionStmt dStmt = (DefinitionStmt) call;
			Abstraction abs = source.deriveNewAbstraction(dStmt.getLeftOp(), call);
			if (pathTracking == PathTrackingMethod.ForwardTracking)
				((AbstractionWithPath) abs).addPathElement(call);
		}
		
		return set;
	}
	
}
