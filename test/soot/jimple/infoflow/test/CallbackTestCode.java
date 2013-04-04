package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;


public class CallbackTestCode {
	public void checkLocation(){
		Container con = new Container();
		MyLocationListener locationListener = new MyLocationListener(con); 
		
		locationListener.onLocationChanged();
		con.publish();
		
	}
	
	private class MyLocationListener {  
		
		private Container con;
		
		public MyLocationListener(Container con) {
			this.con = con;
		}
		
		  public void onLocationChanged() {  
			  con.field = TelephonyManager.getDeviceId();
		  }
	}
	
	private class Container{
		private String field = "";

		public void publish() {
			ConnectionManager cm = new ConnectionManager();
			cm.publish(field);
		}
		
	}
	
	
	//---------- 2nd try:
	
	public void tryNext(){
		Activity a = new Activity();
		a.onCreate();
		LocListener l2 = new LocListener(a);
		l2.set();
		a.send();
	}
	
	private class Activity{
		String field;
		public void onCreate(){
			LocListener l = new LocListener(this);
			
		}
		
		public void send(){
			ConnectionManager cm = new ConnectionManager();
			cm.publish(field);
		}
		
		
	}
	
	private class LocListener{
		private Activity parent;
		public LocListener(Activity a){
			parent = a;
		}
		
		public void set(){
			parent.field = TelephonyManager.getDeviceId();
		}
		
	}
	
}
