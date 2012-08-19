package soot.jimple.infoflow.test;

import java.util.Arrays;
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
	
	
	public void arrayAsFieldOfClass(){
		String tainted = TelephonyManager.getDeviceId();
		
		String[] array = new String[2];
		array[1] = "neutral";
		array[0] = tainted;
		
		ClassWithFinal<String> c = new ClassWithFinal<String>(array);
		String[] taintTaint = c.a;
		String y = taintTaint[0];
		String not = taintTaint[1];
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

}
