package soot.jimple.infoflow.data;

public class Source {
	private String method;
	private String theClass;
	
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getTheClass() {
		return theClass;
	}
	public void setTheClass(String theClass) {
		this.theClass = theClass;
	}
	
	public Source(String newMethod, String newClass){
		method = newMethod;
		theClass = newClass;
	}
}
