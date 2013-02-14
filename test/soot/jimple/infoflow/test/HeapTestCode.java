package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

public class HeapTestCode {

	public void methodTest1(){
		String tainted = TelephonyManager.getDeviceId();
		 new AsyncTask().execute(tainted);
	}
	
	protected class AsyncTask{
		public Worker mWorker;
		public FutureTask mFuture;
		
		public AsyncTask(){
			mWorker = new Worker(){
				public void call(){
					ConnectionManager cm = new ConnectionManager();
					cm.publish(mParams);
				}
			};
			mFuture = new FutureTask(mWorker);
		}
		
		public void execute(String t){
			mWorker.mParams = t;
	        //shortcut (no executor used):
			//exec.execute(mFuture);
			mFuture.run();
		}
		
	}
	
	protected class Worker{
		public String mParams;
		
		public void call(){
		}
	}
	protected class FutureTask{
		private final Worker wo;
		public FutureTask(Worker w){
			wo = w;
		}
		public void run(){
			wo.call();
		}
	}
}
