package soot.jimple.infoflow.util;

import soot.Local;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;

public class DataTypeHandler {
	
	public static boolean isFieldRefOrArrayRef(Value val){
		if(val == null){
			return false;
		}
		if(val instanceof FieldRef || (val instanceof Local && ((Local)val).getType() instanceof ArrayRef)){
			return true;
		}
		return false;
	}

}
