package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.util.ListExampleWrapper;

public class WrapperListTests extends JUnitTests {
	ListExampleWrapper listWrapper = new ListExampleWrapper();

	@Test
    public void concreteArrayListPos0Test(){
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void concreteWriteReadPos0Test()>");
    	infoflow.setTaintWrapper(listWrapper);
    	infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
    }
	
	@Test
    public void concreteArrayListPos1Test(){
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void concreteWriteReadPos1Test()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
    }
    
    @Test
    public void concreteArrayListNegativeTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void concreteWriteReadNegativeTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
  }
    
    @Test
    public void listTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void writeReadTest()>");
    	infoflow.setTaintWrapper(listWrapper);
    	infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));		
    }
    
    @Test
    public void listIteratorTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void iteratorTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);		
    }
    
    @Test
    public void listsubListTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void subListTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);		
    }
    
    @Test
    public void concreteLinkedListNegativeTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void linkedListConcreteWriteReadNegativeTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		infoflow.setTaintWrapper(listWrapper);
		negativeCheckInfoflow(infoflow);
//		assertFalse(errOutputStream.toString().contains("untaintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));

    }
    
    @Test
    public void concreteLinkedListTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void linkedListConcreteWriteReadTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));	
    }
    
    @Test
    public void writeReadLinkedListTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void linkedListWriteReadTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));
		
    }
    
    
    @Test
    public void concreteLinkedListIteratorTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void linkedListIteratorTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));
		
    }
    
    @Test
    public void subLinkedListTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void linkedListSubListTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    


    @Test
    public void stackGetTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void concreteWriteReadStackGetTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));		
    }
    
    @Test
    public void stackPeekTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void concreteWriteReadStackPeekTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);	
    }
    
    @Test
    public void stackPopTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void concreteWriteReadStackPopTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);	
    }
    
    @Test
    public void stackNegativeTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ListTestCode: void concreteWriteReadStackNegativeTest()>");
    	infoflow.setTaintWrapper(listWrapper);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);	
    }

}