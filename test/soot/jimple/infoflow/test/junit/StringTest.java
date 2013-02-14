package soot.jimple.infoflow.test.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class StringTest extends JUnitTests {


    @Test
    public void concatTest1(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcat1()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("result contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void concatTest1b(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcat1b()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("var#2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void concatTest1c(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcat1c(java.lang.String)>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("result contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void concatTest2(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcat2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("post contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));		
    }
    
    @Test
    public void concatPlusTest1(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcatPlus1()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("result contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("post contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));	
    }
    
    @Test
    public void concatPlusTest2(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringConcatPlus2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);	
    }
    
    @Test
    public void concatValueOfTest1(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodValueOf()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("result contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("result2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));		
    }
    
    @Test
    public void concatToStringTest1(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodtoString()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("result contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("result2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));		
    }
    
    @Test
    public void stringBuilderTest1(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder1()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
		//assertTrue((errOutputStream.toString().contains("test.<java.lang.String: char[] value> contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()") ||
		
    }
    
    @Test
    public void stringBuilderTest2(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue((errOutputStream.toString().contains("test.<java.lang.String: char[] value> contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()") ||
//				errOutputStream.toString().contains("test contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()")));
		
    }
    
    @Test
    public void stringBuilderTest4(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder4(java.lang.String)>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);	
    }
    
    @Test
    public void stringBuilderTest3(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilder3()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);	
    }
    
    @Test
    public void stringBuilderTestURL() throws FileNotFoundException{
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void methodStringBuilderURL()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
//		assertTrue(errOutputStream.toString().contains("test contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
//		assertTrue(errOutputStream.toString().contains("url contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void testURL() throws IOException{
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void testURL()>");
    	OutputStream streamOut = 
                new FileOutputStream(new File("urldebug.txt"));
		PrintStream writerOut = new PrintStream(streamOut);
		//System.setErr(writerOut);
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		
		checkInfoflow(infoflow);
		writerOut.close();
		streamOut.close();
//		assertTrue(errOutputStream.toString().contains("url contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		
    }
    
    @Test
    public void test133(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.IndexOutOfBoundsException: void method()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
    }
    
    @Test
    public void testcharArray(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void getChars()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow);
    }
    
    @Test
    public void testPrototyp() throws FileNotFoundException{
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.StringTestCode: void originalFromPrototyp()>");
    	infoflow.computeInfoflow(path, epoints,sources, sinks);
//		assertTrue((errOutputStream.toString().contains("this.<soot.jimple.infoflow.test.StringTestCode: java.lang.String URL> contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()") ||
//		errOutputStream.toString().contains("this contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()")));
    }

}
