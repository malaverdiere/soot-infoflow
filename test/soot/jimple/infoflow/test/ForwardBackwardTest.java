package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class ForwardBackwardTest {

	public void testMethod(){
		C c = new C();
		A a = new A();
		c.h = TelephonyManager.getDeviceId();
		G b = a.g;
		foo(a, c);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(b.f);
	}
	
	public void foo(A z, C t){
		G x = z.g;
		String w = t.h;
		x.f = w;
	}
	
	private class A{
		public G g;
	}
	
	private class C{
		public String h;
	}
	private class G{
		public String f;
	}
}
