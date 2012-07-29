package soot.jimple.infoflow.test;
public class onChange {

	public String dontcallmeoncreate(String test2){
		return test2;
	}
	
	public void onChange1(String str){
		String b = str;
		String l = dontcallmeoncreate(b);
		String v = l;
		TestNoMain tnm = new TestNoMain();
		String l2 = tnm.dontcallmeoncreate("");
		b = l2;
	}

}
