package soot.jimple.infoflow.test;

import java.util.concurrent.SynchronousQueue;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class QueueTestCode {
	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		SynchronousQueue<String> q = new SynchronousQueue<String>();
		q.add(tainted);
		//not implemented for SynchronousQueue: q.element();
		String taintedElement3 = q.poll();
	
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement3);	
	}
	
	public void concreteWriteReadNegativeTest(){
		String tainted = TelephonyManager.getDeviceId();
		String untainted = "Hello world!";
		SynchronousQueue<String> q = new SynchronousQueue<String>();
		SynchronousQueue<String> p = new SynchronousQueue<String>();
		q.add(tainted);
		p.add(untainted);
		String taintedElement = q.poll();
		String untaintedElement = p.poll();
		taintedElement.toString();
		ConnectionManager cm = new ConnectionManager();
		cm.publish(untaintedElement);
	}
	
	
	
	

}
