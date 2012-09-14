package soot.jimple.infoflow.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * tests list tainting, use with DumbsourceManager
 * @author Christian
 *
 */
public class SetTestCode {
	

	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		HashSet<String> set = new HashSet<String>();
		//list.add("neutral");
		set.add(tainted);
		
		//String taintedElement = list.get(1);
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = set.iterator().next();
		//todo: check contains?
		
		String complete =taintedElement2;
		
	}
	
	
	public void writeReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		Set<String> set = new HashSet<String>();
		set.add(tainted);
		String taintedElement = set.iterator().next();
		
		String complete = taintedElement;
		
	}
	
	public void iteratorTest(){
		String tainted = TelephonyManager.getDeviceId();
		HashSet<String> set = new HashSet<String>();
		set.add("neutral");
		set.add(tainted);
		
		Iterator<String> it = set.iterator();
		String taintedElement = it.next();
		String taintedElement2 = it.next();
		
		String complete = taintedElement.concat(taintedElement2);
	}
	

	
}
