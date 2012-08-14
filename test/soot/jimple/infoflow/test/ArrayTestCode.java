package soot.jimple.infoflow.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import soot.jimple.infoflow.test.android.TelephonyManager;

/**
 * tests list tainting, use with DumbsourceManager
 * @author Christian
 *
 */
public class ArrayTestCode {
	
	static String[] staticTainted;
	transient String[] transTainted;
	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		String[] array = new String[2];
		array[0] = "neutral";
		array[1] = tainted;
		
		String taintedElement = array[1];
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = array[0];
		
		staticTainted = array;
		
		String[] tainted123 = staticTainted;
		
		transTainted = array;
		
		String[] tainted456 = transTainted;
		
		String[] alsoTainted = Arrays.copyOf(array, 100);
		
		
		String complete = taintedElement.concat(taintedElement2);
		String x = alsoTainted[1];
	}
	
	
	
	public void ArrayAsListTest(){
		//Arrays.asList(a)
		String tainted = TelephonyManager.getDeviceId();
		List<String> list = new ArrayList<String>();
		list.add("neutral");
		list.add(tainted);
		
		List<String> subList = list.subList(1, 1);
		String taintedElement = subList.get(0);
	}

}
