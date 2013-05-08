package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class MapTests extends JUnitTests {

    @Test
    public void mapPos0Test(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void writeReadPos0Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void mapPos1Test(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void writeReadPos1Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);	
    }

    @Test
    public void concreteMapPos0Test(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteReadPos0Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
    }
    
    @Test
    public void concreteMapPos1Test(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteReadPos1Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
    }
    
    @Test
    public void concreteMapTest2(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteRead2Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
    }
    
    @Test
    public void mapIteratorTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void iteratorTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void mapEntryTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void entryTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);	
    }
    
    @Test
    public void concreteTableTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteReadTableTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
    }
    
    @Test
    public void concreteNegativeTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteReadNegativeTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }

}
