package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.TelephonyManager;

public class MailTest {

	public void method(){
		Foobar x= new Foobar(); x.field = TelephonyManager.getDeviceId();
		Foobar y = new Foobar(); y.field = "not_tainted";
	
	 String x2 = foo(x);
	 String y2 = foo(y);
	 
	 String comp = x2.concat(y2);
	}
	
	String foo(Foobar obj) {
	   return obj.field;
	}
	 

	 private class Foobar{
		 String field;
	 
	 
	 }
	
}

