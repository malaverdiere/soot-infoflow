package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * Target code for tests that require the taint tracking engine to correctly
 * evaluate the semantics of primitive operations.
 * 
 * @author Steven Arzt
 */
public class OperationSemanticTestCode {
	
	public void baseTestCode() {
		String deviceID = TelephonyManager.getDeviceId();
		ConnectionManager cm = new ConnectionManager();
		int len = deviceID.length();
		cm.publish(len);
	}

	public void mathTestCode() {
		String deviceID = TelephonyManager.getDeviceId();
		ConnectionManager cm = new ConnectionManager();
		int len = deviceID.length();
		len = len - len;
		cm.publish(len);
	}

}
