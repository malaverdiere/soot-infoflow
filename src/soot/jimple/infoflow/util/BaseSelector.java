package soot.jimple.infoflow.util;

import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JIfStmt;

public class BaseSelector {
	//we want to keep ArrayRef for objects on the right side of the assignment
	public static Value selectBase(Value val, boolean right){
		//we taint base of array instead of array elements
		if (val instanceof ArrayRef && !right) {
			return selectBase(((ArrayRef) val).getBase(), right);
		}
		
		if (val instanceof JCastExpr) {
			return selectBase((((JCastExpr) val).getOpBox().getValue()), right);
		}
		
		return val;
	}
	
	public static Unit selectBase(Unit u){
		if (u instanceof JIfStmt) {
			return BaseSelector.selectBase(((soot.jimple.internal.JIfStmt) u).getTarget());	
		}
		return u;
		
	}

}
