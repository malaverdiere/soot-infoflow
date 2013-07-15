package soot.jimple.infoflow.test.utilclasses;


public class C2static {
	public static C1static cfield;
	
	public C2static(String c){
		cfield = new C1static(c);
	}
}
