package soot.jimple.infoflow.permissionmap;

import java.util.ArrayList;
import java.util.List;

public class DumbSourceManager extends SourceManager {

	private List<String> sourceMethodNames;
	
	public DumbSourceManager(){
		sourceMethodNames = new ArrayList<String>();
		sourceMethodNames.add("getPassword");
		sourceMethodNames.add("getUserData");
		sourceMethodNames.add("getDeviceId");
	}
	
	@Override
	public boolean isSourceMethod(Class<?> methodClass, String methodName) {
		//TODO: check class, too.
		//check: methodClass.getClasses() and look for interfaces/classes
		System.out.println(methodClass.toString());
		if(sourceMethodNames.contains(methodName)){
			return true;
		}
		return false;
	}

}
