package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.InfoflowResults;
import soot.jimple.infoflow.config.SootConfigForTest;
import soot.jimple.infoflow.InfoflowResults;
import soot.jimple.infoflow.config.SootConfigForTest;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;

public abstract class JUnitTests {


    protected static String path;
    protected static List<String> sources;
    protected static List<String> sinks;
    protected static final String sinkString = "<soot.jimple.infoflow.test.android.ConnectionManager: void publish(java.lang.String)>";
    protected static final String sinkStringInt = "<soot.jimple.infoflow.test.android.ConnectionManager: void publish(int)>";
    protected static final String sourceString = "staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()";
    protected static boolean taintWrapper = false;
    protected static boolean debug = true;
   
    @BeforeClass
    public static void setUp() throws IOException
    {
    	 File f = new File(".");
    	 path = f.getCanonicalPath() + File.separator + "bin";
        
        sources = new ArrayList<String>();

        sources.add("<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>");
        sources.add("<soot.jimple.infoflow.test.android.AccountManager: java.lang.String[] getUserData(java.lang.String)>");
        sources.add("<soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>");
        sinks = new ArrayList<String>();
        sinks.add("<soot.jimple.infoflow.test.android.ConnectionManager: void publish(java.lang.String)>");
        sinks.add("<soot.jimple.infoflow.test.android.ConnectionManager: void publish(int)>");
    }
    
    @Before
    public void resetSootAndStream() throws IOException{
    	 soot.G.reset();
    	 System.gc();
    	 
    }
    
    protected void checkInfoflow(Infoflow infoflow){
		  if(infoflow.isResultAvailable()){
				InfoflowResults map = infoflow.getResults();
				assertTrue(map.containsSink(sinkString));
				assertTrue(map.isPathBetween(sinkString, sourceString));
			}else{
				fail("result is not available");
			}
	  }
    
    protected void negativeCheckInfoflow(Infoflow infoflow){
		  if(infoflow.isResultAvailable()){
				InfoflowResults map = infoflow.getResults();
				assertFalse(map.containsSink(sinkString));
			}else{
				fail("result is not available");
			}
	  }
    
    protected Infoflow initInfoflow(){
    	Infoflow result = new Infoflow();
    	Infoflow.setDebug(debug);
    	SootConfigForTest testConfig = new SootConfigForTest();
    	result.setSootConfig(testConfig);
    	if(taintWrapper){
    		EasyTaintWrapper easyWrapper;
			try {
				easyWrapper = new EasyTaintWrapper(new File("EasyTaintWrapperSource.txt"));
				result.setTaintWrapper(easyWrapper);
			} catch (IOException e) {
				System.err.println("Could not initialized Taintwrapper:");
				e.printStackTrace();
			}
    		
    	}
    	return result;
    }
    
}