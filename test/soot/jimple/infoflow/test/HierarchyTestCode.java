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

}
