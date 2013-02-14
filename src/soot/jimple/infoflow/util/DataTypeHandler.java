package soot.jimple.infoflow.util;

import soot.PrimType;
import soot.RefType;
import soot.SootField;
import soot.Value;

public class DataTypeHandler {
	
	public static boolean isPrimTypeOrString(Value val){
		if(val == null){
			return false;
		}
		if(val.getType() instanceof PrimType){
			return true;
		}
		if(val.getType() instanceof RefType && ((RefType)val.getType()).getClassName().equals("java.lang.String")){
			return true;
		}
		return false;
	}
	
	public static boolean isPrimTypeOrString(SootField val){
		if(val.getType() instanceof PrimType){
			return true;
		}
		if(val.getType() instanceof RefType && ((RefType)val.getType()).getClassName().equals("java.lang.String")){
			return true;
		}
		return false;
	}

}
