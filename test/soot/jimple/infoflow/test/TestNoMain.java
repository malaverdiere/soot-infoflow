package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.AccountManager;

public class TestNoMain {
	
	public String result = "";
	
	public void functionCallThis(){
		String l = this.function1();
		result = l;
		
	}
	
	public void functionCallOnObject(){
		TestNoMain nm = new TestNoMain();
		String l = nm.function1();
		result = l;
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
		new TestNoMain().functionCallThis();
	}
}
