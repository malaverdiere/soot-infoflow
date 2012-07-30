package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.TelephonyManager;

public class TestNoMain {

	public String dontcallmeoncreate(){
		String deviceID = TelephonyManager.getDeviceId();
		return deviceID;
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
