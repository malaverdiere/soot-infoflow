package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;
import soot.jimple.infoflow.test.utilclasses.C1static;
import soot.jimple.infoflow.test.utilclasses.C2static;

public class MailTest {

	public void methodTainted() {
		O x = new O();
		x.field = TelephonyManager.getDeviceId();
		O y = new O();
		y.field = "not_tainted";

		String x2 = foo(x);
		String y2 = foo(y);
		y2.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(x2);
	}
	public void methodNotTainted() {
		O x = new O();
		x.field = TelephonyManager.getDeviceId();
		O y = new O();
		y.field = "not_tainted";

		String x2 = foo(x);
		String y2 = foo(y);
		x2.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(y2);
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
		untaint.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taint);
	}
	
	public void method2NotTainted() {
		String tainted = TelephonyManager.getDeviceId();
		O a = new O();
		//Fehler wenn:
		O b = new O();
		
		foo(a, tainted);
		foo(b, "untainted");
		
		String taint = a.field;
		String untaint = b.field;
		taint.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(untaint);
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
		d.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(c);
	}
	
	public void method3NotTainted(){
		String tainted = TelephonyManager.getDeviceId();
		String untainted = "hallo welt";
		List1 a = new List1();
		List1 b = new List1();
		a.add(tainted);
		b.add(untainted);
		
		String c = a.get();
		String d = b.get();
		c.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(d);
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
	
	
	
	
	
	
	
	//advanced:
	
	public void method4(){
		String tainted = TelephonyManager.getDeviceId();
		C2 c = new C2(tainted); 
		C2 d = c;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(d.cfield.field1);
	}
	
	public void method5(){
		String tainted = TelephonyManager.getDeviceId();
		
		C2 c = new C2("test");
		C1 changes = c.cfield;
		c.cfield.field1 = tainted;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(changes.field1);
		
		
	}
	
	private class C1{
		String field1;
		
		public C1(String c){
			field1 = c;
		}
	}
	
	private class C2{
		C1 cfield;
		
		public C2(String c){
			cfield = new C1(c);
		}
	}
	
	public void method6(){
		String tainted = TelephonyManager.getDeviceId();
		
		@SuppressWarnings("unused")
		C2static c = new C2static("test");
		C1static.field1 = tainted;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(C2static.cfield.getField());
		
		
	}
	
	
}
