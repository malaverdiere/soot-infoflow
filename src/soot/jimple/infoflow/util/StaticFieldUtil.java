package soot.jimple.infoflow.util;

import soot.SootClass;
import soot.jimple.StaticFieldRef;

public class StaticFieldUtil {

	public static SootClass getCorrectClassForStaticField(StaticFieldRef ref){
		SootClass classOfField = ref.getField().getDeclaringClass();
		if(classOfField.declaresFieldByName(ref.getField().getName())){
			return classOfField;
			
		}else{
			while(classOfField.hasSuperclass()){
				classOfField = classOfField.getSuperclass();
				if(classOfField.declaresFieldByName(ref.getField().getName())){
					return classOfField;
				}
			}
		}
		return null;
	}
}
