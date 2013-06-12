package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class HierarchyTestCode {
	
	public void taintedOutputTest(){
		String tainted = TelephonyManager.getDeviceId();
		String untainted  = "Caption:";
		Outer o1 = new Outer();
		o1.next = new Inner();
		o1.next.field = tainted;
		
		Outer o2 = new Outer();
		o2.next = new Inner();
		o2.next.field = untainted;
		
		String taintedOutput = o1.next.field;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedOutput);
	}
	
	public void untaintedOutputTest(){
		String tainted = TelephonyManager.getDeviceId();
		String untainted  = "Caption:";
		Outer o1 = new Outer();
		o1.next = new Inner();
		o1.next.field = tainted;
		
		Outer o2 = new Outer();
		o2.next = new Inner();
		o2.next.field = untainted;
		
		String taintedOutput = o2.next.field;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedOutput);
	}
	
	private class Outer{
		public Inner next;
	}
	
	private class Inner{
		public String field;
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

}
