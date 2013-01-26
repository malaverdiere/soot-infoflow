package soot.jimple.infoflow.test.utilclasses;

public class C1static {
	public static String field1;
	
	public C1static(String c){
		field1 = c;
	}
	
	public String getField(){
		return field1;
	}
}
