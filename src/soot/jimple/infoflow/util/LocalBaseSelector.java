package soot.jimple.infoflow.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.BinopExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.UnopExpr;
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
		
		// Check for unary operators like "not" or "length"
		if (val instanceof UnopExpr)
			return selectBase(((UnopExpr) val).getOp());

		return val;
	}

	public static Set<Value> selectBaseList(Value val){
		if (val instanceof BinopExpr) {
			Set<Value> set = new HashSet<Value>();
			BinopExpr expr = (BinopExpr) val;
			set.add(expr.getOp1());
			set.add(expr.getOp2());
			return set;
		}
		return Collections.singleton(selectBase(val));
	}

}
