package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.AccountManager;

public class TestNoMain {

	public String dontcallmeoncreate(){
		AccountManager aManager = new AccountManager();
		String test = aManager.getPassword();
		return test;
	}
	
	public void onChange(String str){
		String b = str;
		String l = dontcallmeoncreate();
		String v = l;
	}
	
	public void onCreate(String test) {
		String c = test;
		String x = c;
	}
}
