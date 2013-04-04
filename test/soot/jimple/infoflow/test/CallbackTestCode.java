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
}
