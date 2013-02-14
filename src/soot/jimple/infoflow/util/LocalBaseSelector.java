package soot.jimple.infoflow.util;

import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.internal.JCastExpr;

public class LocalBaseSelector {
	
	public static Value selectBase(Value val){
		//we taint base of array instead of array elements
		if (val instanceof ArrayRef) {
			return selectBase(((ArrayRef) val).getBase());
		}
		
		//we taint base object instead of fields:
		if(val instanceof InstanceFieldRef){
			return selectBase(((InstanceFieldRef)val).getBase());
		}
		
		if (val instanceof JCastExpr) {
			return selectBase((((JCastExpr) val).getOp()));
		}
		
		return val;
	}

}
