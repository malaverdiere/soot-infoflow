package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;
import soot.jimple.infoflow.test.utilclasses.ClassWithFinal;
import soot.jimple.infoflow.test.utilclasses.ClassWithStatic;

public class OtherTestCode {
	
	public void staticTest(){
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
		//this way it does not work:
		cm.publish(concat1.concat(concat2).concat(concat3));
		//this way, it works:
//		cm.publish(concat1);
//		cm.publish(concat2);
//		cm.publish(concat3);
		
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
}
