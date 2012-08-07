package soot.jimple.infoflow.source;
import java.util.ArrayList;
import java.util.List;


public class MethodName {
	private String methodName;
	private List<String> classes;
	public String getMethodName() {
		return methodName;
	}
	
	public MethodName(){
		classes = new ArrayList<String>();
	}
	
	public MethodName(String m, String cl){
		classes = new ArrayList<String>();
		methodName = m;
		classes.add(cl);
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public List<String> getClasses() {
		return classes;
	}
	public void setClasses(List<String> classes) {
		this.classes = classes;
	}
	
	public void addClass(String classString){
		if(classes == null){
			classes = new ArrayList<String>();
		}
		if(!classes.contains(classString)){
			classes.add(classString);
		}
	}
	
}
