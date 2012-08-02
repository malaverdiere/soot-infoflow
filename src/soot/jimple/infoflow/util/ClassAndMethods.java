package soot.jimple.infoflow.util;

import java.util.ArrayList;
import java.util.List;

public class ClassAndMethods {
	private String className;
	private List<String> methodNames;
	private boolean nomain;
	
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public List<String> getMethodNames() {
		return methodNames;
	}
	public void setMethodNames(List<String> methodNames) {
		this.methodNames = methodNames;
	}
	public void addMethodName(String methodName){
		if(methodNames == null){
			methodNames = new ArrayList<String>();
		}
		if(!methodNames.contains(methodName)){
			methodNames.add(methodName);
		}
	}
	public boolean isNomain() {
		return nomain;
	}
	public void setNomain(boolean nomain) {
		this.nomain = nomain;
	}
}
