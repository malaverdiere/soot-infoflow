package soot.jimple.infoflow.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;


public class HTTPTestCode {
	
	public void testURL() throws MalformedURLException{
		String urlString = "http://www.google.de/?q="+ TelephonyManager.getDeviceId();
		URL url = new URL(urlString);
		ConnectionManager cm = new ConnectionManager();
		cm.publish(url.toString());
		}

	public void method1() throws IOException{
		String imei = TelephonyManager.getDeviceId();
    	URL url = new URL(imei);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setReadTimeout(10000 /* milliseconds */);
//        conn.setConnectTimeout(15000 /* milliseconds */);
//        conn.setRequestMethod("GET");
//        conn.setDoInput(true);
        // Starts the query
//        conn.connect();
        ConnectionManager cm = new ConnectionManager();
		cm.publish(conn.toString());
	}
}
