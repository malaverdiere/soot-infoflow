package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.AccountManager;
import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class OnChangeClass {


	
	public void onChange1(){
		String b = " ";
		AccountManager manager = new AccountManager();
		b = manager.getPassword();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(b);
	}
	
	public String onChangeWithReturn(){
		String t1 = TelephonyManager.getDeviceId();
		String t3 = t1;
		return t3;
	}

}
