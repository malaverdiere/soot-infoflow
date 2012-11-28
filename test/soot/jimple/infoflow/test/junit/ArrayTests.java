package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class ArrayTests extends JUnitTests {

	  @Test
	    public void arrayTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ArrayTestCode: void concreteWriteReadTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>"));
			assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke"));
			assertTrue(errOutputStream.toString().contains("alsoTainted contains value from staticinvoke"));
			assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke"));
			assertTrue(errOutputStream.toString().contains("tainted123 contains value from staticinvoke"));
			assertTrue(errOutputStream.toString().contains("tainted789 contains value from staticinvoke"));
			assertTrue(errOutputStream.toString().contains("tainted456 contains value from staticinvoke"));
			
	    }
	  
	  @Test
	    public void arrayCopyTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ArrayTestCode: void copyTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			assertTrue(errOutputStream.toString().contains("copyTainted contains value from staticinvoke"));
			
	    }
	    
	    @Test
	    public void arrayAsFieldOfClassTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ArrayTestCode: void arrayAsFieldOfClass()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			
			assertTrue(errOutputStream.toString().contains("y contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
	    	
	    }
	    
	    @Test
	    public void arrayAsListTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ArrayTestCode: void arrayAsListTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			
			assertTrue(errOutputStream.toString().contains("<java.util.Arrays$ArrayList: java.lang.Object[] a> contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
			assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke"));
	    }
	    
	    @Test
	    public void concreteNegativeTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ArrayTestCode: void concreteWriteReadNegativeTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			
			assertFalse(errOutputStream.toString().contains("untaintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
			assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));

	    }
	    
}
