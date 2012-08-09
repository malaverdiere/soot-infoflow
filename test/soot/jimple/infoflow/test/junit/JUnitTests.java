package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import soot.jimple.infoflow.IInfoflow;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.cmdInfoflow;
import soot.jimple.infoflow.data.AnalyzeClass;
import soot.jimple.infoflow.data.AnalyzeMethod;

public class JUnitTests {

  //  private static ByteArrayOutputStream sysOutputStream;
    private static ByteArrayOutputStream errOutputStream;

    @BeforeClass
    public static void setUp()
    {
     //   sysOutputStream = new ByteArrayOutputStream();
       // System.setOut(new PrintStream(sysOutputStream));
        errOutputStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errOutputStream));
    }

    @AfterClass
    public static void tearDown()
    {
        System.setOut(System.out);
        System.setErr(System.err);
    }
    
    
    
    @Test
    public void OnChangeClass1Cmd() { 
    	String[] args = new String[]{"-class", "soot.jimple.infoflow.test.OnChangeClass", "-methods", "onChange1", "-nomain"};
    	
    	cmdInfoflow.main(args);
    	 
        assertTrue(errOutputStream.toString().contains("t3 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 
    

    @Test
    public void NoMainFunctionCallThis() { 
    		
    	AnalyzeClass analyzeClass = null;
		analyzeClass = new AnalyzeClass();
		analyzeClass.setNameWithPath("soot.jimple.infoflow.test.TestNoMain");
		AnalyzeMethod aMethod = new AnalyzeMethod();
		aMethod.setName("functionCallOnObject");
		aMethod.setReturnType("java.lang.String");
		List<AnalyzeMethod> methodList = new ArrayList<AnalyzeMethod>();
		methodList.add(aMethod);
		analyzeClass.setHasMain(false);
		analyzeClass.setMethods(methodList);
		IInfoflow infoflow = new Infoflow();
		List<AnalyzeClass> classList = new ArrayList<AnalyzeClass>();
		classList.add(analyzeClass);
		infoflow.computeInfoflow("", classList,null, null);
    	 
        assertTrue(errOutputStream.toString().contains("l contains value from")); 
//        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
//        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
//        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 

    
    @Test
    public void NoMainFunctionCallThisCmd() throws InterruptedException { 
    	String[] args = new String[]{"-class", "soot.jimple.infoflow.test.TestNoMain", "-methods", "functionCallThis", "-nomain"};
    	Thread.sleep(1500);
    	cmdInfoflow.main(args);
    	 
        assertTrue(errOutputStream.toString().contains("l contains value from")); 
//        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
//        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
//        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 
    
    @Test
    public void NoMainFunctionCallOnObjectCmd() { 
    	String[] args = new String[]{"-class", "soot.jimple.infoflow.test.TestNoMain", "-methods", "functionCallOnObject"};
    	
    	cmdInfoflow.main(args);
    	 
        assertTrue(errOutputStream.toString().contains("l contains value from")); 
//        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
//        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
//        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 
    
    

    
    @Test
    public void TestIf(){
    	
    }


}
