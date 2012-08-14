package soot.jimple.infoflow.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * tests list tainting, use with DumbsourceManager
 * @author Christian
 *
 */
public class ListTestCode {
	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		ArrayList<String> list = new ArrayList<String>();
		list.add("neutral");
		list.add(tainted);
		
		String taintedElement = list.get(1);
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = list.get(0);
		
		String complete = taintedElement.concat(taintedElement2);
		
	}
	
	
	public void writeReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new ArrayList<String>();
		list.add("neutral");
		list.add(tainted);
		
		String taintedElement = list.get(1);
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = list.get(0);
		
		String complete = taintedElement.concat(taintedElement2);
		
	}
	
	public void iteratorTest(){
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new ArrayList<String>();
		list.add("neutral");
		list.add(tainted);
		
		Iterator<String> it = list.iterator();
		String taintedElement = it.next();
		String taintedElement2 = it.next();
	}
	
	public void subListTest(){
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new ArrayList<String>();
		list.add("neutral");
		list.add(tainted);
		
		List<String> subList = list.subList(1, 1);
		String taintedElement = subList.get(0);
	}

}
