package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.TelephonyManager;

public class MailTest {

	public void method() {
		O x = new O();
		x.field = TelephonyManager.getDeviceId();
		O y = new O();
		y.field = "not_tainted";

		String x2 = foo(x);
		String y2 = foo(y);

		x2.concat("");
		y2.concat("");
	}

	String foo(O obj) {
		return obj.field;
	}

	private class O {
		String field;

	}
	


	
	public void method2() {
		String tainted = TelephonyManager.getDeviceId();
		O a = new O();
		//Fehler wenn:
		O b = new O();
		
		foo(a, tainted);
		foo(b, "untainted");
		
		String taint = a.field;
		String untaint = b.field;
		
		taint.concat(untaint);
		}
	
	void foo(O obj, String s){
		obj.field = s;
	}
	
	
	
	public void method3(){
		String tainted = TelephonyManager.getDeviceId();
		String untainted = "hallo welt";
		List1 a = new List1();
		List1 b = new List1();
		a.add(tainted);
		b.add(untainted);
		
		String c = a.get();
		String d = b.get();
		
		c.concat(d);
	}
	
	private class List1 {
		private String field;
		
		public void add(String e){
			field = e;
		}
		
		public String get(){
			return field;
		}

	}
	
	
	
}
