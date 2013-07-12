package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;
import soot.jimple.infoflow.test.utilclasses.ClassWithStatic;

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
		

	public void staticTest(){
		String tainted = TelephonyManager.getDeviceId();
		ClassWithStatic static1 = new ClassWithStatic();
		static1.setTitle(tainted);
		ClassWithStatic static2 = new ClassWithStatic();
		String alsoTainted = static2.getTitle();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(alsoTainted);
	}
	
	public static void static2Test(){
		String tainted = TelephonyManager.getDeviceId();
		ClassWithStatic static1 = new ClassWithStatic();
		static1.setTitle(tainted);
		ClassWithStatic static2 = new ClassWithStatic();
		String alsoTainted = static2.getTitle();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(alsoTainted);
		}
}
