package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;
import soot.jimple.infoflow.test.utilclasses.ClassWithField;

public class StringTestCode {
	
	public void methodSubstring(){
		String tainted = TelephonyManager.getDeviceId();
		String result = tainted.substring(1, 2);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result);
	}
	
	public void methodStringLowerCase(){
		String tainted = TelephonyManager.getDeviceId();
		String result = tainted.toLowerCase();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result);
	}
	
	public void methodStringUpperCase(){
		String tainted = TelephonyManager.getDeviceId();
		String result = tainted.toUpperCase();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result);
	}
	
	public void methodStringConcat1(){
		String pre = "pre";
		String tainted = TelephonyManager.getDeviceId();
		String result = pre.concat(tainted);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result);
	}
	
	public void methodStringConcat1b(){
		String var = "2";
		String tainted = TelephonyManager.getDeviceId();
		var = var.concat(tainted);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(var);
	}
	
	public void methodStringConcat1c(String var){
		String tainted = TelephonyManager.getDeviceId();
		String result = var.concat(tainted);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result);
	}
	
	public void methodStringConcat2(){
		String pre = "pre";
		String tainted = TelephonyManager.getDeviceId();
		String post = tainted.concat(pre);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(post);
	}
	
	public void methodStringConcatPlus1(){
		String pre = "pre";
		String tainted = TelephonyManager.getDeviceId();;
		String post =  tainted + pre;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(post);
	}
	
	public void methodStringConcatPlus2(){
		String pre = "pre";
		String tainted = TelephonyManager.getDeviceId();
		String result = pre + tainted;
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result);
	}
	
	public void methodValueOf(){
		String tainted = TelephonyManager.getDeviceId();
		String result = String.valueOf(tainted);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result);
	}
	
	public void methodtoString(){
		String tainted = TelephonyManager.getDeviceId();
		String result2 = tainted.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(result2);
	}
	
	public void methodStringBuffer1(){
		StringBuffer sb = new StringBuffer(TelephonyManager.getDeviceId());
		sb.append("123");
		String test = sb.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(test);
	}
	
	public void methodStringBuffer2(){
		StringBuffer sb = new StringBuffer("12");
		sb.append(TelephonyManager.getDeviceId());
		String test = sb.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(test);
	}
	
	public void methodStringBuilder1(){
		StringBuilder sb = new StringBuilder(TelephonyManager.getDeviceId());
		//sb.append("123");
		String test = sb.toString();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(test);
	}
	
	public void methodStringBuilder2(){
		StringBuilder sb = new StringBuilder();
		sb.append(TelephonyManager.getDeviceId());
		String test = sb.toString();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(test);	
	}
	
	public void methodStringBuilder3(){
		String tainted = TelephonyManager.getDeviceId();
		StringBuilder sb = new StringBuilder("Hello World");
        sb.append(tainted);
        String test = sb.toString();
        ConnectionManager cm = new ConnectionManager();
		cm.publish(test);
	}

	
	public void methodStringBuilder4(String a){
		String b = a;
		fieldc.field = b;
		String tainted = TelephonyManager.getDeviceId();
		StringBuilder sb = new StringBuilder("Hello World");
        sb.append(tainted);
        String test = sb.toString();
        String c = fieldc.field;
        ConnectionManager cm = new ConnectionManager();
		cm.publish(test);
		cm.publish(c);
		
		ClassWithField cf = new ClassWithField();
		cf.field = fieldc.field;
	}

	public void getChars(){
		//like: str.getChars(0, len, value, count);
		String t = TelephonyManager.getDeviceId();
		char[] x = new char[t.length()];
		t.getChars(0, t.length(), x, 0);
	
		ConnectionManager cm = new ConnectionManager();
		cm.publish(String.valueOf(x));
	}


//	private String imei;
	private String URL = "http://www.google.de/?q=";
	private ClassWithField fieldc = new ClassWithField();
//	public void originalFromPrototyp(){
//		imei = TelephonyManager.getDeviceId();
//		URL = URL.concat(imei);
//	}

}
