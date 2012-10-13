package soot.jimple.infoflow.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * tests list tainting, use with DumbsourceManager
 * @author Christian
 *
 */
public class SetTestCode {
	

	
	public void concreteWriteReadHashTest(){
		String tainted = TelephonyManager.getDeviceId();
		HashSet<String> set = new HashSet<String>();
		set.add(tainted);
		String taintedElement2 = set.iterator().next();
		//TODO: check contains?
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement2);
		
	}
	
	public void concreteWriteReadTreeTest(){
		String tainted = TelephonyManager.getDeviceId();
		TreeSet<String> set = new TreeSet<String>();
		set.add("neutral");
		set.add(tainted);
		
		String taintedElement = set.last();
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = set.iterator().next();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
		cm.publish(taintedElement2);
		
	}
	
	public void concreteWriteReadLinkedTest(){
		String tainted = TelephonyManager.getDeviceId();
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		set.add("neutral");
		set.add(tainted);
		Iterator<String> it = set.iterator();
		String taintedElement = it.next();
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = it.next();

		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
		cm.publish(taintedElement2);
		
	}
	
	public void writeReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		Set<String> set = new HashSet<String>();
		set.add(tainted);
		String taintedElement = set.iterator().next();

		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
		
	}
	
	public void iteratorTest(){
		String tainted = TelephonyManager.getDeviceId();
		HashSet<String> set = new HashSet<String>();
		set.add("neutral");
		set.add(tainted);
		
		Iterator<String> it = set.iterator();
		String taintedElement = it.next();
		String taintedElement2 = it.next();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
		cm.publish(taintedElement2);
	}
	
	public void concreteWriteReadNegativeTest(){
		String tainted = TelephonyManager.getDeviceId();
		//TreeSet<String> notRelevantList = new TreeSet<String>();
		TreeSet<String> list = new TreeSet<String>();
		list.add("neutral");
		//notRelevantList.add(tainted);
		//String taintedElement = notRelevantList.get(0);
		String untaintedElement = list.first();
		String complete =untaintedElement;
		complete = tainted;//.concat(taintedElement);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(complete);
	}
	
}
