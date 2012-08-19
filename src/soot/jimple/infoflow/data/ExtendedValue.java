package soot.jimple.infoflow.data;

import soot.Value;

public class ExtendedValue {
	private Value value;
	private boolean isStatic;
	private boolean isGlobalVar;
	
	public ExtendedValue(Value newVal, boolean globalVar, boolean staticVar){
		value = newVal;
		isStatic = staticVar;
		isGlobalVar = globalVar;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public boolean isGlobalVar() {
		return isGlobalVar;
	}

	public void setGlobalVar(boolean isGlobalVar) {
		this.isGlobalVar = isGlobalVar;
	}
}
