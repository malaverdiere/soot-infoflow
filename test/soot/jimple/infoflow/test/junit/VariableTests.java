package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.cmdInfoflow;

public class VariableTests extends JUnitTests {

    @Test
    public void OnChangeClass1Cmd() { 
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OnChangeClass: void onChange1()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
//    	String[] args = new String[]{"-entrypoints", "<soot.jimple.infoflow.test.OnChangeClass: void onChange1()>", "-path", path};
    	
//    	cmdInfoflow.main(args);
    	 
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
		infoflow.computeInfoflow(path, epoints,sources, sinks);
    	 
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
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.TestNoMain: java.lang.String functionCallOnObject()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		
//    	String[] args = new String[]{"-entrypoints", "<soot.jimple.infoflow.test.TestNoMain: java.lang.String functionCallOnObject()>", "-path", path};
    	
//    	cmdInfoflow.main(args);
    	 
        assertTrue(errOutputStream.toString().contains("l contains value from")); 
//        assertTrue(errOutputStream.toString().contains("t1 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")); 
//        assertTrue(errOutputStream.toString().contains("v contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
//        assertTrue(errOutputStream.toString().contains("b contains value from virtualinvoke manager.<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>()")); 
    }
    
}