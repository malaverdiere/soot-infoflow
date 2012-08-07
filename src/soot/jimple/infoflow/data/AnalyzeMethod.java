package soot.jimple.infoflow.data;

import java.util.List;

public class AnalyzeMethod {
	private String name; //for example: "doSomething"
	private String returnValue; //full path, for example "java.lang.String"
	private List<String> parameters; //each with full path, for example: "java.lang.String"

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
}
