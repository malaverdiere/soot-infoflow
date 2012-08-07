package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import soot.jimple.infoflow.cmdInfoflow;

public class JUnitTests {

    private static ByteArrayOutputStream sysOutputStream;
    private static ByteArrayOutputStream errOutputStream;

    @BeforeClass
    public static void setUp()
    {
        sysOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(sysOutputStream));
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
    public void OnChangeClass1() { 
    	String[] args = new String[]{"-class", "soot.jimple.infoflow.test.OnChangeClass", "-methods", "onChange1", "-nomain"};
    	
    	cmdInfoflow.main(args);
    	 
        assertTrue(errOutputStream.toString().contains("t3 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 
    
    @Test
    public void NoMainFunctionCallThis() { 
    	String[] args = new String[]{"-class", "soot.jimple.infoflow.test.TestNoMain", "-methods", "functionCallThis", "-nomain"};
    	
    	cmdInfoflow.main(args);
    	 
        assertTrue(errOutputStream.toString().contains("l contains value from")); 
//        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
//        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
//        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 

    } 
    
    @Test
    public void NoMainFunctionCallOnObject() { 
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
