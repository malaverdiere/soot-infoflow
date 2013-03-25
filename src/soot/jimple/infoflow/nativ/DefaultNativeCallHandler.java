package soot.jimple.infoflow.nativ;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.PrimType;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.AbstractInfoflowProblem.PathTrackingMethod;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionWithPath;

public class DefaultNativeCallHandler extends NativeCallHandler {
	
	PathTrackingMethod pathTracking = PathTrackingMethod.NoTracking;
	
	public void setPathTracking(PathTrackingMethod method) {
		this.pathTracking = method;
	}

	public Set<Abstraction> getTaintedValues(Stmt call, Abstraction source, List<Value> params){
		HashSet<Abstraction> set = new HashSet<Abstraction>();
		
		//check some evaluated methods:
		
		//arraycopy:
		//arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        //Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array.
		if(call.getInvokeExpr().getMethod().toString().contains("arraycopy")){
			if(params.get(0).equals(source.getAccessPath().getPlainValue())){
				if (pathTracking == PathTrackingMethod.ForwardTracking)
					set.add(new AbstractionWithPath(params.get(2),
							(AbstractionWithPath) source));
				else
					set.add(new Abstraction(params.get(2), source));
			}
		}else{
			//generic case: add taint to all non-primitive datatypes:
			for (int i = 0; i < params.size(); i++) {
				Value argValue = params.get(i);
				if (!(argValue.getType() instanceof PrimType)) {
					if (pathTracking == PathTrackingMethod.ForwardTracking)
						set.add(new AbstractionWithPath(argValue,
								(AbstractionWithPath) source));
					else
						set.add(new Abstraction(argValue, source));
				}
			}	
		}
		//add the  returnvalue:
		if(call instanceof DefinitionStmt){
			DefinitionStmt dStmt = (DefinitionStmt) call;
			if (pathTracking == PathTrackingMethod.ForwardTracking)
				set.add(new AbstractionWithPath(dStmt.getLeftOp(),
						(AbstractionWithPath) source));
			else
				set.add(new Abstraction(dStmt.getLeftOp(), source));
		}
		
		return set;
	}
}
