package soot.jimple.infoflow.test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * 
 * @author Christian
 *
 */
public class VectorTestCode {
	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		Vector<String> v = new Vector<String>();
		v.add("neutral");
		v.add(tainted);
		
		String taintedElement = v.get(1);
		//because whole collection is tainted, even untainted elements are tainted if they are fetched 
		String taintedElement2 = v.lastElement();
		
		
		String complete =taintedElement2.concat(taintedElement);
		
	}
	
	
	public void iteratorTest(){
		String tainted = TelephonyManager.getDeviceId();
		Collection<String> v = new Vector<String>();
		v.add("neutral");
		v.add(tainted);
		Iterator it = v.iterator();
		String taintedElement = (String) it.next();
		Object obj = it.next();
		String taintedElement2 = (String) obj;
		
		String complete = taintedElement.concat(taintedElement2);
		
	}
	
	
	public void concreteIteratorTest(){
		String tainted = TelephonyManager.getDeviceId();
		Vector<String> v = new Vector<String>();
		v.add(tainted);
		
		Iterator<String> it = v.iterator();
		String taintedElement = it.next();
		
		String complete = taintedElement;
	}
	
	
}
