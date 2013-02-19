package soot.jimple.infoflow.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.BinopExpr;
import soot.jimple.UnopExpr;
import soot.jimple.internal.JCastExpr;

public class BaseSelector {
	
	//we want to keep ArrayRef for objects on the right side of the assignment
	public static Value selectBase(Value val, boolean right){
		//we taint base of array instead of array elements
		if (val instanceof ArrayRef && !right) {
			return selectBase(((ArrayRef) val).getBase(), right);
		}
		
		if (val instanceof JCastExpr) {
			return selectBase(((JCastExpr) val).getOpBox().getValue(), right);
		}
		
		// Check for unary operators like "not" or "length"
		if (val instanceof UnopExpr)
			return selectBase(((UnopExpr) val).getOp(), right);
		
		return val;
	}

	public static Set<Value> selectBaseList(Value val, boolean right){
		if (val instanceof BinopExpr) {
			Set<Value> set = new HashSet<Value>();
			BinopExpr expr = (BinopExpr) val;
			set.add(expr.getOp1());
			set.add(expr.getOp2());
			return set;
		}
		return Collections.singleton(selectBase(val, right));
	}
	
}
