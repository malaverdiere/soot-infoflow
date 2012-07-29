package soot.jimple.infoflow.test;

public class TestNoMain {

	public String dontcallmeoncreate(String test2){
		return test2;
		//System.out.println(f);
	}
	
	public void onChange(String str){
		String b = str;
		String l = dontcallmeoncreate(b);
		String v = l;
	}
	
	public void onCreate(String test) {
		String c = test;
		String x = c;
	}
}
