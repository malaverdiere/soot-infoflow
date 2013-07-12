package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * tests constant tainting
 * @author Christian
 *
 */
public class ConstantTestCode {
	
	static final String tainted = TelephonyManager.getDeviceId();
	static final String[] staticArray = new String[1];
	final String[] fieldArray = new String[1];

	public void easyConstantFieldTest(){
		ConnectionManager cm = new ConnectionManager();
		cm.publish(tainted);
	}
	
	public void easyConstantVarTest(){
		final String e = TelephonyManager.getDeviceId();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(e);
	}
	
	public void constantFieldArrayTest(){
		String tainted =  TelephonyManager.getDeviceId();
		staticArray[0] = tainted;
		fieldArray[0] = tainted;
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(staticArray[0]);
		cm.publish(fieldArray[0]);
		
	}
	
	public void constantFieldTest(){
		ConstantClass c = new ConstantClass();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(c.e);
	}
	
	class ConstantClass{
		final String e;
		public ConstantClass(){
			e = TelephonyManager.getDeviceId();
		}
	}

}
