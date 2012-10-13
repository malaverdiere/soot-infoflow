package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;


public class VectorTests extends JUnitTests{

	 @Test
	    public void vectorRWTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void concreteWriteReadTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
			assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
			
	    }

	    @Test
	    public void vIteratorTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void iteratorTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
			assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));	
	    }
	    
	    @Test
	    public void concreteVIteratorTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void concreteIteratorTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));	
	    }
	    
	    @Test
	    public void concreteNegativeTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void concreteWriteReadNegativeTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			
			assertFalse(errOutputStream.toString().contains("untaintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
			assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));

	    }
	    
}
