package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.AccountManager;
import soot.jimple.infoflow.test.android.ConnectionManager;

public class TestNoMain {
	
	public String result = "";
	
	public void functionCallThis(){
		String l = this.function1();
		result = l;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(l);
	}
	
	public String functionCallOnObject(){
		TestNoMain nm = new TestNoMain();
		String l = nm.function2("", "");
		result = l;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(l);
		return l;
	}

	public String function1(){
		AccountManager aManager = new AccountManager();
		String test = aManager.getPassword();
		return test;
	}
	
	public String function2(String a, String b){
		AccountManager aManager = new AccountManager();
		String test = a.concat(b);
		test = aManager.getPassword();
		return test;
	}
	
	
	public static void foo() {
		new TestNoMain().functionCallThis();
	}
}
