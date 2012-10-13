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

}
