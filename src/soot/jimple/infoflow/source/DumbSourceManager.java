package soot.jimple.infoflow.source;

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
		if(sourceMethodNames.contains(methodName)){
			return true;
		}
		return false;
	}

}
