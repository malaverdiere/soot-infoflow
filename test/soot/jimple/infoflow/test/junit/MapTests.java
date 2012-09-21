package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class MapTests extends JUnitTests {

    @Test
    public void mapTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void writeReadTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }

    @Test
    public void concreteMapTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteReadTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void mapIteratorTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void iteratorTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }

}
