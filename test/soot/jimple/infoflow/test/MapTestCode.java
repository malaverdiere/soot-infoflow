package soot.jimple.infoflow.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * 
 * @author Christian
 *
 */
public class MapTestCode {
	
	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		HashMap<String, String> map = new HashMap<String, String>();
		//list.add("neutral");
		map.put("tainted", tainted);
		
		//String taintedElement = list.get(1);
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = map.get("tainted");
		
		
		String complete =taintedElement2;
		
	}
	
	
	public void writeReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		Map<String, String> map = new HashMap<String, String>();
		map.put("neutral", "neutral");
		map.put("tainted", tainted);
		
		String taintedElement = map.get(1);
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = map.get(0);
		
		String complete = taintedElement.concat(taintedElement2);
		
	}
	
	public void entryTest(){
		
		
	}
	
	public void iteratorTest(){
		String tainted = TelephonyManager.getDeviceId();
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("neutral", "neutral");
		map.put("tainted", tainted);
		
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		String taintedElement = it.next().getValue(); //entry is not enough!
		String taintedElement2 = it.next().getValue();
		
		String complete = taintedElement.concat(taintedElement2);
	}
	
	
}
