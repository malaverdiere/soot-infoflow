package soot.jimple.infoflow.data;

import java.util.List;

public class AnalyzeClass {
	private String nameWithPath;
	private List<AnalyzeMethod> methods;
	private boolean hasMain;
	//TODO: evtl.: isStatic?

	public String getNameWithPath() {
		return nameWithPath;
	}

	public void setNameWithPath(String nameWithPath) {
		this.nameWithPath = nameWithPath;
	}

	public List<AnalyzeMethod> getMethods() {
		return methods;
	}

	public void setMethods(List<AnalyzeMethod> methods) {
		this.methods = methods;
	}

	public boolean isHasMain() {
		return hasMain;
	}

	public void setHasMain(boolean hasMain) {
		this.hasMain = hasMain;
	}
}
