package soot.jimple.infoflow.test;

import java.util.concurrent.SynchronousQueue;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class QueueTestCode {
	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		SynchronousQueue<String> q = new SynchronousQueue<String>();
		q.add(tainted);
		//not implemented for SynchronousQueue:
		//String taintedElement = q.element();
		String taintedElement3 = q.poll();
	
		
		ConnectionManager cm = new ConnectionManager();
		//cm.publish(taintedElement);
		cm.publish(taintedElement3);	
	}
	
	//TODO: negative test
	
	

}
