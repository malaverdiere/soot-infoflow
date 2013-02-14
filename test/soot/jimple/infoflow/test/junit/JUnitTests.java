package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;

import soot.jimple.infoflow.Infoflow;

public abstract class JUnitTests {


    protected static String path;
    protected static List<String> sources;
    protected static List<String> sinks;
    protected static final String sinkString = "<soot.jimple.infoflow.test.android.ConnectionManager: void publish(java.lang.String)>";
    protected static final String sourceString = "staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()";
    protected static boolean local = false;
   
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

    }
    
    @Before
    public void resetSootAndStream() throws IOException{
    	 soot.G.reset();
    	 System.gc();
    	 
    }
    
    protected void checkInfoflow(Infoflow infoflow){
		  if(infoflow.isResultAvailable()){
				HashMap<String, List<String>> map = infoflow.getResults();
				assertTrue(map.containsKey(sinkString));
				assertTrue(map.get(sinkString).contains(sourceString));
			}else{
				fail("result is not available");
			}
	  }
    
    protected void negativeCheckInfoflow(Infoflow infoflow){
		  if(infoflow.isResultAvailable()){
				HashMap<String, List<String>> map = infoflow.getResults();
				assertFalse(map.containsKey(sinkString));
			}else{
				fail("result is not available");
			}
	  }
    
    protected Infoflow initInfoflow(){
    	Infoflow result = new Infoflow();
    	result.setLocalInfoflow(local);
    	return result;
    }
    
}