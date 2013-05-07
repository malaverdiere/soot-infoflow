package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class StaticTestCode {
	public static String im;
	public void staticInitTest(){
		String tainted1 = TelephonyManager.getDeviceId();
		im = tainted1;
		StaticInitClass1 st = new StaticInitClass1();
		st.printFalse();
	}
	
	
	public static class StaticInitClass1{
		static{
			ConnectionManager cm = new ConnectionManager();
			cm.publish(im);	
		}
		
		public void printFalse(){
			ConnectionManager cm = new ConnectionManager();
			cm.publish("False");
		}
	}
		

}
