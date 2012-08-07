package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.AccountManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class OnChangeClass {


	
	public void onChange1(){
		String t1 = TelephonyManager.getDeviceId();
		String b = " ";
		AccountManager manager = new AccountManager();
		b = manager.getPassword();
		String v = b;
		String t3 = t1;
	}
	
	public String onChangeWithReturn(){
		String t1 = TelephonyManager.getDeviceId();
		String b = " ";
		AccountManager manager = new AccountManager();
		b = manager.getPassword();
		String v = b;
		String t3 = t1;
		return t3;
	}

}
