package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class LengthTestCode{
	
	public void easy1(){
		String taint = TelephonyManager.getDeviceId();
		Firstclass f = new Firstclass();
		f.data.secretValue = taint;
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(f.data.publicValue);	
	}
	
	public void method1(){
		String taint = TelephonyManager.getDeviceId();
		Firstclass f = new Firstclass();
		f.data.secretValue = taint;
		f.data.publicValue = "PUBLIC";
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(f.data.publicValue);	
		
	}
	
	public void method2(){
		String taint = TelephonyManager.getDeviceId();
		Firstclass f = new Firstclass();
		f.data.secretValue = taint;
		f.data.publicValue = "PUBLIC";
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(f.data.secretValue);	
		
	}
	
	class Firstclass{
		SecondClass data;
		
		public Firstclass(){
			data = new SecondClass();
		}
		
	}
	
	class SecondClass{
		public String secretValue;
		public String publicValue;
	}

}
