package soot.jimple.infoflow.data;

import soot.SootMethod;

public class SootMethodAndClass {
	private SootMethod sootMethod;
	private String classString;
	
	public SootMethodAndClass(){
		
	}
	
	public SootMethodAndClass(SootMethod method, String cString){
		sootMethod = method;
		classString = cString;
	}
	
	public SootMethod getSootMethod() {
		return sootMethod;
	}
	public void setSootMethod(SootMethod sootMethod) {
		this.sootMethod = sootMethod;
	}
	public String getClassString() {
		return classString;
	}
	public void setClassString(String classString) {
		this.classString = classString;
	}
}
