package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.AccountManager;

public class onChange {


	
	public void onChange1(String str){
		String b = str;
		AccountManager manager = new AccountManager();
		b = manager.getPassword();
		String v = b;
	}

}
