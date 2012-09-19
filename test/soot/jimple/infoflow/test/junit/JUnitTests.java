package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.cmdInfoflow;

public class JUnitTests {

    private static ByteArrayOutputStream errOutputStream;
    private static PrintStream pStream;
    private static String path;// = "F:\\master\\workspace\\soot-infoflow\\bin";

    @BeforeClass
    public static void setUp() throws IOException
    {
    	 File f = new File(".");
    	 path = f.getCanonicalPath() + File.separator + "bin";
    	 
        errOutputStream = new ByteArrayOutputStream();
        pStream = new PrintStream(errOutputStream);
        //remove comment from the following line before running tests:
       System.setErr(pStream);
    }

    @AfterClass
    public static void tearDown()
    {
        System.setErr(System.err);
       
    }
    
    @Before
    public void resetSootAndStream() throws IOException{
    	 soot.G.reset();
    	 pStream.flush();
    	 errOutputStream.flush();
    	 System.gc();
    }
    
    
    @Test //should work
    public void arrayTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ArrayTestCode: void concreteWriteReadTest()>");
		infoflow.computeInfoflow(path, epoints,new ArrayList<String>(), new ArrayList<String>());
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>"));
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke"));
		assertTrue(errOutputStream.toString().contains("tainted456 contains value from staticinvoke"));
		assertTrue(errOutputStream.toString().contains("tainted123 contains value from staticinvoke"));
		assertTrue(errOutputStream.toString().contains("alsoTainted contains value from staticinvoke"));
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke"));
    }
    
    @Test
    public void arrayAsFieldOfClassTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ArrayTestCode: void arrayAsFieldOfClass()>");
		infoflow.computeInfoflow(path, epoints,null, null);
    	
    }
    
    @Test
    public void staticTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void staticTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		assertTrue(errOutputStream.toString().contains("alsoTainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("<soot.jimple.infoflow.test.utilclasses.ClassWithStatic: java.lang.String staticTitle> contains value from staticinvoke"));

    }
    
    @Test
    public void ConstructorFinalClassTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void genericsfinalconstructorProblem()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		
		assertTrue(errOutputStream.toString().contains("alsoTainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("this.<soot.jimple.infoflow.test.utilclasses.ClassWithFinal: java.lang.String b> contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId("));
    	
    }
    
    @Test
    public void arrayAsListTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.ArrayTestCode: void arrayAsListTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		
		assertTrue(errOutputStream.toString().contains("<java.util.Arrays$ArrayList: java.lang.Object[] a> contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke"));
    }
    
    
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
    
    //FIXME: this runs way too long @Test
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
    
    
    @Test
    public void setTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void writeReadTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void concreteSetTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void setIteratorTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void iteratorTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    
    @Test
    public void OnChangeClass1Cmd() { 
    	String[] args = new String[]{"-entrypoints", "<soot.jimple.infoflow.test.OnChangeClass: void onChange1()>", "-path", path};
    	
    	cmdInfoflow.main(args);
    	 
        assertTrue(errOutputStream.toString().contains("t3 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 
    

    @Test
    public void NoMainFunctionCallThis() { 
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.TestNoMain: void functionCallThis()>");
		infoflow.computeInfoflow(path, epoints,null, null);
    	 
        assertTrue(errOutputStream.toString().contains("l contains value from")); 
//        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
//        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
//        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 

    
    @Test
    public void NoMainFunctionCallThisCmd() throws InterruptedException { 
    	String[] args = new String[]{"-entrypoints", "<soot.jimple.infoflow.test.TestNoMain: void functionCallThis()>", "-path", path};
    	Thread.sleep(1500);
    	cmdInfoflow.main(args);
    	 
        assertTrue(errOutputStream.toString().contains("l contains value from")); 
//        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
//        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
//        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 
    
    @Test
    public void NoMainFunctionCallOnObjectCmd() { 
    	String[] args = new String[]{"-entrypoints", "<soot.jimple.infoflow.test.TestNoMain: java.lang.String functionCallOnObject()>", "-path", path};
    	
    	cmdInfoflow.main(args);
    	 
        assertTrue(errOutputStream.toString().contains("l contains value from")); 
//        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
//        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
//        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 
    
}
