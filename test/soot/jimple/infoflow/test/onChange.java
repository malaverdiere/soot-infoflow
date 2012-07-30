package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.AccountManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class onChange {


	
	public void onChange1(String str){
		String t1 = TelephonyManager.getDeviceId();
		String b = str;
		AccountManager manager = new AccountManager();
		b = manager.getPassword();
		String v = b;
		String t3 = t1;
	}

}
