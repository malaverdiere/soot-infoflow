package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class ListTests extends JUnitTests {

	@Test
    public void concreteArrayListTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void concreteWriteReadTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		//FIXME:
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));

    }
    
    @Test
    public void concreteArrayListNegativeTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void concreteWriteReadNegativeTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		
		assertFalse(errOutputStream.toString().contains("untaintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));

    }
    
    @Test
    public void concreteLinkedListTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void linkedListConcreteWriteReadTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));
		
    }
    
    @Test
    public void writeReadLinkedListTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void linkedListWriteReadTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));
		
    }
    
    
    @Test
    public void concreteLinkedListIteratorTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void linkedListIteratorTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));
		
    }
    
    @Test
    public void subLinkedListTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void linkedListSubListTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void listTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void writeReadTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);

		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));		
    }

}