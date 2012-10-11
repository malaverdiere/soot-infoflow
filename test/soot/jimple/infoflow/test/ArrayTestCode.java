package soot.jimple.infoflow.test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import soot.jimple.infoflow.test.android.TelephonyManager;
import soot.jimple.infoflow.test.utilclasses.ClassWithFinal;

/**
 * tests list tainting, use with DumbsourceManager
 * @author Christian
 *
 */
public class ArrayTestCode {
	
	static String[] staticTainted;
	transient String[] transTainted;
	String[] globalTainted;
	
	public void concreteWriteReadTest(){
		String tainted = TelephonyManager.getDeviceId();
		String[] array = new String[2];
		array[0] = "neutral";
		array[1] = tainted;
		
		String taintedElement = array[1];
		//because whole list is tainted, even untainted elements are tainted if they are fetched from the list
		String taintedElement2 = array[0];
		globalTainted = array;
		staticTainted = array;
		String[] tainted123 = staticTainted;
		transTainted = array;
		String[] tainted456 = transTainted;
		String[] tainted789 = globalTainted;
		String[] alsoTainted = Arrays.copyOf(array, 100);
		String complete = taintedElement.concat(taintedElement2);
		complete = tainted456[0].concat(tainted123[0]).concat(tainted789[0]);
		String x = alsoTainted[1];
		
	}
	
	
	public void arrayAsFieldOfClass(){
		String tainted = TelephonyManager.getDeviceId();
		
		String[] array = new String[2];
		array[1] = "neutral";
		array[0] = tainted;
		
		ClassWithFinal<String> c = new ClassWithFinal<String>(array);
		String[] taintTaint = c.a;
		String y = taintTaint[0];
		String not = taintTaint[1]; //but is also tainted because wohle array is tainted
	}
	
	public void arrayAsListTest(){
		String tainted = TelephonyManager.getDeviceId();
		String[] array = new String[1];
		array[0] = tainted;
		List<String> list = Arrays.asList(array);
		String taintedElement = list.get(0);
		String dummyString = taintedElement;
		
		//TODO: list is missing!
	}
	
	public void concreteWriteReadNegativeTest(){
		String tainted = TelephonyManager.getDeviceId();
		//LinkedList<String> notRelevantList = new LinkedList<String>();
		String[] array = new String[2];
		array[0] = "neutral";
		array[1] = "neutral2";
		
		//notRelevantList.add(tainted);
		//String taintedElement = notRelevantList.get(0);
		String untaintedElement = array[0];
		String complete =untaintedElement;
		complete = tainted;//.concat(taintedElement);
	}

}
