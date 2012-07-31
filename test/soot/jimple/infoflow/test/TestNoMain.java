package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.AccountManager;

public class TestNoMain {
	
	public String result = "";
	
	public void onChange(String str){
		String b = str;
		//works:
		//TestNoMain nm = new TestNoMain();
		//String l = nm.function1();
		String l = this.function1();
		String v = l;
		result = v.concat(b);
	}

	public String function1(){
		AccountManager aManager = new AccountManager();
		String test = aManager.getPassword();
		return test;
	}
	
	public void onCreate(String test) {
		String c = test;
		String x = c;
	}
	
	public static void foo() {
		new TestNoMain().onChange("");
	}
}
