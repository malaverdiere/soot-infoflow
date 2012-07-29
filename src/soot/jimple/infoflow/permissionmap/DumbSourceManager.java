package soot.jimple.infoflow.permissionmap;

public class DumbSourceManager extends SourceManager {

	@Override
	public boolean isSourceMethod(String methodName) {
		if(methodName.startsWith("dont")){
			return true;
		}
		return false;
	}

}
