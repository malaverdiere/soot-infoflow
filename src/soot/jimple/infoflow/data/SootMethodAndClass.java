package soot.jimple.infoflow.data;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Type;


public class SootMethodAndClass {
	private final String methodName;
	private final String className;
	private final String returnType;
	private final List<String> parameters;
	
	public SootMethodAndClass
			(String methodName,
			String className,
			String returnType,
			List<String> parameters){
		this.methodName = methodName;
		this.className = className;
		this.returnType = returnType;
		this.parameters = parameters;
	}
	
	public SootMethodAndClass(SootMethod sm) {
		this.methodName = sm.getName();
		this.className = sm.getDeclaringClass().getName();
		this.returnType = sm.getReturnType().toString();
		this.parameters = new ArrayList<String>();
		for (Type p: sm.getParameterTypes())
			this.parameters.add(p.toString());
	}
	
	public SootMethodAndClass(SootMethodAndClass methodAndClass) {
		this.methodName = methodAndClass.methodName;
		this.className = methodAndClass.className;
		this.returnType = methodAndClass.returnType;
		this.parameters = new ArrayList<String>(methodAndClass.parameters);
	}

	public String getMethodName() {
		return this.methodName;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public String getReturnType() {
		return this.returnType;
	}
	
	public List<String> getParameters() {
		return this.parameters;
	}
	
	public String getSubSignature() {
		String s = (this.returnType.length() == 0 ? "" : this.returnType + " ") + this.methodName + "(";
		for (int i = 0; i < this.parameters.size(); i++) {
			if (i > 0)
				s += ",";
			s += this.parameters.get(i).trim();
		}
		s += ")";
		return s;
	}

	public String getSignature() {
		String s = "<" + this.className + ": " + (this.returnType.length() == 0 ? "" : this.returnType + " ")
				+ this.methodName + "(";
		for (int i = 0; i < this.parameters.size(); i++) {
			if (i > 0)
				s += ",";
			s += this.parameters.get(i).trim();
		}
		s += ")>";
		return s;
	}

}
