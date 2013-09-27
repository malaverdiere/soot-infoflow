package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * Code for testing the implicit flow feature
 * @author Steven Arzt
 *
 */
public class ImplicitFlowTestCode {
	
	public void simpleTest() {
		String tainted = TelephonyManager.getDeviceId();
		String foo = "";
		if (tainted == "123")
			foo = "x";
		ConnectionManager cm = new ConnectionManager();
		cm.publish(foo);
	}

	public void simpleNegativeTest() {
		String tainted = TelephonyManager.getDeviceId();
		String foo = "";
		if (foo.equals("")) {
			if (tainted == "123")
				tainted = "Hello";
			foo = "x";
		}
		ConnectionManager cm = new ConnectionManager();
		cm.publish(foo);
	}

	public void simpleOverwriteTest() {
		String tainted = TelephonyManager.getDeviceId();
		String foo = "";
		if (tainted == "123") {
			tainted = "Hello";
			foo = "x";
		}
		ConnectionManager cm = new ConnectionManager();
		cm.publish(foo);
	}

	public void switchTest() {
		int secret = TelephonyManager.getIMEI();
		String foo = "";
		switch (secret) {
		case 1:
			foo = "x";
			break;
		}
		ConnectionManager cm = new ConnectionManager();
		cm.publish(foo);
	}
	
	public void convertTest() {
		int secret = TelephonyManager.getIMEI();
		String imei = Integer.toString(secret);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(imei);
	}

	public void sinkTest() {
		int secret = TelephonyManager.getIMEI();
		ConnectionManager cm = new ConnectionManager();
		if (secret == 42)
			cm.publish("Secret is 42");
	}
	
	private boolean lookup(int i) {
		return i == 42;
	}

	public void returnTest() {
		int secret = TelephonyManager.getIMEI();
		ConnectionManager cm = new ConnectionManager();
		if (lookup(secret))
			cm.publish("Secret is 42");
	}
	
	private void doPublish() {
		ConnectionManager cm = new ConnectionManager();
		cm.publish("Secret is 42");
	}

	public void callTest() {
		int secret = TelephonyManager.getIMEI();
		if (secret == 42)
			doPublish();
	}
	
	private void doSomething() {
		int i = 0;
		while (i % 2 == 0)
			i++;
	}

	public void callTest2() {
		int secret = TelephonyManager.getIMEI();
		if (secret == 42) {
			doSomething();
			secret = 0;
			doPublish();
		}
	}

	public void negativeCallTest() {
		int secret = TelephonyManager.getIMEI();
		int other = 42;
		if (other == 42)
			doPublish();
		if (secret == 42)
			other = 1;
	}
	
	private void runSimpleRecursion(int i) {
		if (i == 0)
			doPublish();
		else runSimpleRecursion(i - 1);
	}
	
	public void recursionTest() {
		int secret = TelephonyManager.getIMEI();
		if (secret == 42)
			runSimpleRecursion(42);
	}

	public void recursionTest2() {
		int secret = TelephonyManager.getIMEI();
		runSimpleRecursion(secret);
	}
	
	private void recurseIndirect(int i) {
		if (i > 0)
			recurseIndirect2(i--);
		else
			doPublish();
	}

	private void recurseIndirect2(int i) {
		recurseIndirect(i);
	}
	
	public void recursionTest3() {
		int secret = TelephonyManager.getIMEI();
		if (secret == 42)
			recurseIndirect(42);
	}

	public void exceptionTest() {
		String tainted = TelephonyManager.getDeviceId();
		try {
			if (tainted == "123")
				throw new RuntimeException("Secret is 42");
		}
		catch (RuntimeException ex) {
			doPublish();
		}
	}

	public void exceptionTest2() {
		String tainted = TelephonyManager.getDeviceId();
		try {
			if (tainted == "123")
				throw new RuntimeException("Secret is 42");
		}
		catch (RuntimeException ex) {
			ConnectionManager cm = new ConnectionManager();
			cm.publish(ex.getMessage());
		}
	}

	public void exceptionTest3() {
		String tainted = TelephonyManager.getDeviceId();
		Throwable t = null;
		if (tainted == "123")
			t = new Throwable();
		if (t != null)
			doPublish();
	}
	
	private int val = 0;
	
	private void fieldAccess() {
		this.val = 3;
	}
	
	public void fieldTest() {
		int secret = TelephonyManager.getIMEI();
		if (secret == 42)
			fieldAccess();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(val);
	}
	
	private static int staticVal = 0;	
	
	private void bar() {
		String x = "Hello World";
		System.out.println(x);
	}
	
	private void staticFieldAccess() {
		staticVal = 42;
		bar();
	}

	public void staticFieldTest() {
		int secret = TelephonyManager.getIMEI();
		if (secret == 42)
			staticFieldAccess();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(staticVal);
	}
	
	private static void staticDoPublish() {
		ConnectionManager cm = new ConnectionManager();
		cm.publish(staticVal);
	}

	private static void conditionalStaticFieldAccess(int i) {
		if (i == 42)
			staticVal = 42;
	}
	
	public void staticFieldTest2() {
		int secret = TelephonyManager.getIMEI();
		conditionalStaticFieldAccess(secret);
		staticDoPublish();
	}
	
	private static class StaticDataClass {
		StaticDataClass2 data;
	}
	
	private static StaticDataClass staticDataClass = new StaticDataClass();
	
	private static class StaticDataClass2 {
		int data;
	}
	
	private static void conditionalStaticClassAccess() {
		ImplicitFlowTestCode.staticDataClass.data.data = 42;
	}

	public void staticFieldTest3() {
		int secret = TelephonyManager.getIMEI();
		if (secret == 42)
			conditionalStaticClassAccess();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(ImplicitFlowTestCode.staticDataClass.data.data);
	}

	public void integerClassTest() {
		// Not an implicit flow, but used to produce a hickup with implicit
		// flows enabled
		int secret = TelephonyManager.getIMEI();
		Integer i = new Integer(secret);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(i);
	}

	public void stringClassTest() {
		// Not an implicit flow, but used to produce a hickup with implicit
		// flows enabled
		String secret = TelephonyManager.getDeviceId();
		int len = secret.length();
		char[] secret2 = new char[len];
		secret.getChars(0, len, secret2, 0);
		ConnectionManager cm = new ConnectionManager();
		//cm.publish(new String(secret2));
		cm.publish(new String(secret));
	}
	
	private void leavesByException() throws Exception {
		throw new Exception("foobar");
	}
	
	private void conditional() throws Exception {
		int secret = TelephonyManager.getIMEI();
		if (secret == 42)
			leavesByException();
	}
	
	public void conditionalExceptionTest() {
		try {
			conditional();
		}
		catch (Exception ex) {
			ConnectionManager cm = new ConnectionManager();
			cm.publish(ImplicitFlowTestCode.staticDataClass.data.data);
		}
	}

}
