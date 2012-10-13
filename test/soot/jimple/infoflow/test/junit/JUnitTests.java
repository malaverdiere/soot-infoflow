package soot.jimple.infoflow.test.junit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class JUnitTests {

    protected static ByteArrayOutputStream errOutputStream;
    protected static PrintStream pStream;
    protected static String path;
    protected static List<String> sources;
    protected static List<String> sinks;

    @BeforeClass
    public static void setUp() throws IOException
    {
    	 File f = new File(".");
    	 path = f.getCanonicalPath() + File.separator + "bin";
    	 
        errOutputStream = new ByteArrayOutputStream();
        pStream = new PrintStream(errOutputStream);
        
        sources = new ArrayList<String>();

        sources.add("<soot.jimple.infoflow.test.android.AccountManager: java.lang.String getPassword()>");
        sources.add("<soot.jimple.infoflow.test.android.AccountManager: java.lang.String[] getUserData(java.lang.String)>");
        sources.add("<soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>");
        
        sinks = new ArrayList<String>();

        sinks.add("<soot.jimple.infoflow.test.android.ConnectionManager: void publish(java.lang.String)>");
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
    
}