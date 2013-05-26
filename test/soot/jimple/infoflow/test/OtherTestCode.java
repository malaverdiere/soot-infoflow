package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;
import soot.jimple.infoflow.test.utilclasses.ClassWithField;
import soot.jimple.infoflow.test.utilclasses.ClassWithField2;
import soot.jimple.infoflow.test.utilclasses.ClassWithFinal;
import soot.jimple.infoflow.test.utilclasses.ClassWithStatic;
import soot.jimple.infoflow.test.utilclasses.D1static;

public class OtherTestCode {

	public void testWithStaticInheritance(){
		D1static obj = new D1static(TelephonyManager.getDeviceId());
		obj.taintIt();
	}
	
	public void testWithFieldInheritance(){
		ClassWithField2 obj = new ClassWithField2(TelephonyManager.getDeviceId());
		obj.taintIt();
	}
	
	public void testWithField(){
		ClassWithField fclass = new ClassWithField();
		fclass.field = TelephonyManager.getDeviceId();
		
//		ClassWithField fclass2 = new ClassWithField();
//		fclass2.field = TelephonyManager.getDeviceId();
//		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(fclass.field);
		cm.publish(fclass.field);
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
	
	public void genericsfinalconstructorProblem(){
		String tainted = TelephonyManager.getDeviceId();
		ClassWithFinal<String> c0 = new ClassWithFinal<String>(tainted, false);
		String alsoTainted = c0.getString();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(alsoTainted);
		
	}

	public void stringConcatTest(){
		String tainted = TelephonyManager.getDeviceId();
		String concat1 = tainted.concat("eins");
		String two = "zwei";
		String concat2 = two.concat(tainted);
		String concat3 = "test " + tainted;
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(concat1.concat(concat2).concat(concat3));	
	}
	
	public void stringConcatTestSmall(){
		String tainted = TelephonyManager.getDeviceId();
		String two = "zwei";
		String one = two.concat(tainted);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(one);
	}
	
	public void stringConcatTestSmall2(){
		String tainted = TelephonyManager.getDeviceId();
		//String two = "zwei";
		String one = tainted.concat("zwei").concat("eins");
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(one);
	}
	
	public void stringConcatTestSmall3(){
		String tainted = TelephonyManager.getDeviceId();
		String concat1 = tainted.concat("eins");
		String two = "zwei";
		String concat2 = two.concat(tainted);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(concat1.concat(concat2).concat("foo"));
	}

	private String deviceId = "";
	
	public interface MyInterface {
		void doSomething();
	}
	
	public void innerClassTest() {
		this.deviceId = TelephonyManager.getDeviceId();
		runIt(new MyInterface() {
			
			@Override
			public void doSomething() {
				ConnectionManager cm = new ConnectionManager();
				cm.publish(deviceId);
			}
			
		});
	}
	
	private void runIt(MyInterface intf) {
		intf.doSomething();
	}
	
	private class HierarchyTest1 {

		private String foo;
				
		public void set() {
			this.foo = TelephonyManager.getDeviceId();
		}
		
		public String get() {
			return this.foo;
		}
			
	}

	private class HierarchyTest2 extends HierarchyTest1 {
		public String foo;
		
		public String get() {
			return this.foo;
		}
	}

	private class HierarchyTest3 extends HierarchyTest1 {
		public String get() {
			return super.get();
		}
	}

	public void classHierarchyTest() {
		HierarchyTest2 ht = new HierarchyTest2();
		ht.set();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(ht.get());
	}
	
	public void classHierarchyTest2() {
		HierarchyTest3 ht = new HierarchyTest3();
		ht.set();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(ht.get());
	}
	
	private String annotate(String data) {
		return "x" + data + "x";
	}

	public void multiCallTest() {
		ConnectionManager cm = new ConnectionManager();
		String deviceId = TelephonyManager.getDeviceId();

		String data = annotate(deviceId);
		cm.publish(data);

		String data2 = annotate(deviceId);
		cm.publish(data2);
	}
	
	public void loopTest() {
		String imei = TelephonyManager.getDeviceId();
		for (int i = 0; i < 10; i++) {
	        ConnectionManager cm = new ConnectionManager();
			cm.publish(imei);
		}
	}
	
	public void dataObjectTest(){
		String imei = TelephonyManager.getDeviceId();

		APIClass api = new APIClass();
		api.testMethod(imei);
        ConnectionManager cm = new ConnectionManager();
		cm.publish(api.getDi());
	}

	class APIClass{
		InternalData d = new InternalData();
		
		public void testMethod(String i){
			//d = i;
			d.setI(i);
		}
		
		String getDi(){
			return d.getI();
		}
	}
	
	class InternalData{
		
		public String getI() {
			return i;
		}

		public void setI(String i) {
			this.i = i;
		}

		String i = "";
	}

}
