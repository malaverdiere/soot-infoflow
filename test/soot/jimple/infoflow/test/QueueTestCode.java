package soot.jimple.infoflow.test;

import java.util.concurrent.SynchronousQueue;

import soot.jimple.infoflow.test.android.TelephonyManager;

public class QueueTestCode {
	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		SynchronousQueue<String> q = new SynchronousQueue<String>();
		q.add(tainted);
		String taintedElement = q.element();
		String taintedElement3 = q.poll();
	
		String complete =taintedElement3.concat(taintedElement);
	//TODO: does not terminate...	
	}
	
	//TODO: negative test
	
	

}
