package soot.jimple.infoflow.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import soot.jimple.infoflow.test.android.ConnectionManager;
import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * tests list tainting, use with DumbsourceManager
 * @author Christian
 *
 */
public class ListTestCode {
	
//	public void concreteWriteReadTest(){
//		//String tainted = TelephonyManager.getDeviceId();
//		Object tainted = TelephonyManager.getDeviceId();
//		ArrayList<Object> list = new ArrayList<Object>();
//		list.add("neutral");
//		list.add(tainted);
//		
//		Object taintedElement = list.get(1);
//		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
//		Object taintedElement2 = list.get(0);
//		
//		String complete = taintedElement.toString()+(taintedElement2);
//		
//	}
	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		ArrayList<String> list = new ArrayList<String>();
		//list.add("neutral");
		list.add(tainted);
		
		//String taintedElement = list.get(1);
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = list.get(0);
		
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement2);
		
	}
	
	public void concreteWriteReadNegativeTest(){
		String tainted = TelephonyManager.getDeviceId();
		//ArrayList<String> notRelevantList = new ArrayList<String>();
		ArrayList<String> list = new ArrayList<String>();
		list.add("neutral");
		//notRelevantList.add(tainted);
		//String taintedElement = notRelevantList.get(0);
		String untaintedElement = list.get(0);
		String complete =untaintedElement;
		complete = tainted;//.concat(taintedElement);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(complete);
	}
	
	
	public void writeReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new ArrayList<String>();
		list.add("neutral");
		list.add(tainted);
		
		String taintedElement = list.get(1);
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = list.get(0);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
		cm.publish(taintedElement2);
		
	}
	
	public void iteratorTest(){
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new ArrayList<String>();
		list.add("neutral");
		list.add(tainted);
		
		Iterator<String> it = list.iterator();
		String taintedElement = it.next();
		String taintedElement2 = it.next();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
		cm.publish(taintedElement2);
	}
	
	public void subListTest(){
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new ArrayList<String>();
		list.add("neutral");
		list.add(tainted);
		
		List<String> subList = list.subList(1, 1);
		String taintedElement = subList.get(0);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
	}

	
	public void linkedListConcreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		LinkedList<String> list = new LinkedList<String>();
		//list.add("neutral");
		list.add(tainted);
		
		//String taintedElement = list.get(1);
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = list.get(0);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement2);
		
	}
	
	public void linkedListConcreteWriteReadNegativeTest(){
		String tainted = TelephonyManager.getDeviceId();
		LinkedList<String> notRelevantList = new LinkedList<String>();
		LinkedList<String> list = new LinkedList<String>();
		list.add("neutral");
		notRelevantList.add(tainted);
		//String taintedElement = notRelevantList.get(0);
		String untaintedElement = list.get(0);

		ConnectionManager cm = new ConnectionManager();
		cm.publish(untaintedElement);
	}
	
	
	public void linkedListWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new LinkedList<String>();
		list.add("neutral");
		list.add(tainted);
		
		String taintedElement = list.get(1);
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = list.get(0);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
		cm.publish(taintedElement2);
		
	}
	
	public void linkedListIteratorTest(){
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new LinkedList<String>();
		list.add("neutral");
		list.add(tainted);
		
		Iterator<String> it = list.iterator();
		String taintedElement = it.next();
		String taintedElement2 = it.next();
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
		cm.publish(taintedElement2);
	}
	
	public void linkedListSubListTest(){
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new LinkedList<String>();
		list.add("neutral");
		list.add(tainted);
		
		List<String> subList = list.subList(1, 1);
		String taintedElement = subList.get(0);
		
		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
	}
	
	public void concreteWriteReadStackTest(){
		String tainted = TelephonyManager.getDeviceId();
		Stack<String> stack = new Stack<String>();
		stack.addElement("neutral");
		stack.push(tainted);
		
		String taintedElement = stack.peek();
		String taintedElement3 = stack.pop();
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = stack.get(0);

		ConnectionManager cm = new ConnectionManager();
		cm.publish(taintedElement);
		cm.publish(taintedElement2);
		cm.publish(taintedElement3);
		
	}
	
}
