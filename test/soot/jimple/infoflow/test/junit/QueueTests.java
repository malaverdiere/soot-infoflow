package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class QueueTests extends JUnitTests {
	
	 @Test
	    public void concreteSynchronousQueueTest(){
	    	Infoflow infoflow = new Infoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.QueueTestCode: void concreteWriteReadTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			assertTrue(errOutputStream.toString().contains("taintedElement3 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
			
	    }

}
