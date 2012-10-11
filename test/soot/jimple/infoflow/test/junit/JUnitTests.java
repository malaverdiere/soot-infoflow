package soot.jimple.infoflow.test.junit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class JUnitTests {

    protected static ByteArrayOutputStream errOutputStream;
    protected static PrintStream pStream;
    protected static String path;

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
    
}