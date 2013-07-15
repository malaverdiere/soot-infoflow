package soot.jimple.infoflow.test.utilclasses;

import soot.jimple.infoflow.test.android.TelephonyManager;

public class C1static {
	public static String field1;
	
	public C1static(String c){
		field1 = c;
	}
	
	public String getField(){
		return field1;
	}
	
	public boolean start(){
		field1 = TelephonyManager.getDeviceId();
		return true;
	}
}
