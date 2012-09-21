package soot.jimple.infoflow.test;

import soot.jimple.infoflow.test.android.TelephonyManager;

public class PTSTestCode {
	
	
	
	public void testPointsToSet(){
		Testclass1 tc1 = new Testclass1();
		String tainted = TelephonyManager.getDeviceId();
		tc1.dummyMethod(tainted);
		String s1 = (String) tc1.getIt();
		String s2 = s1;
	}
	
	private class Testclass1{
		private Object[] elementData;
		
		public Testclass1(){
			elementData = new Object[3];
		}
		
		public boolean dummyMethod(Object obj){
			elementData[0] = obj;
			return true;
		}
		
		public Object getIt(){
			return elementData[0];
		}
		
	}
	
	

}
