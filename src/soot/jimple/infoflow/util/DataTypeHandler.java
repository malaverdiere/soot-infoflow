package soot.jimple.infoflow.util;

import soot.ArrayType;
import soot.Local;
import soot.Value;
import soot.jimple.FieldRef;

public class DataTypeHandler {
	
	public static boolean isFieldRefOrArrayRef(Value val){
		if(val == null){
			return false;
		}
		if(val instanceof FieldRef || (val instanceof Local && ((Local)val).getType() instanceof ArrayType)){
			return true;
		}
		return false;
	}

}
