package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
/**
 * contain tests which check taint propagation for static variables
 */
public class StaticTests extends JUnitTests {
	  
	    @Test
	    public void staticTest(){
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.StaticTestCode: void staticTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
		}
	    
	    @Test
	    public void static2Test(){
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.StaticTestCode: void static2Test()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
	    }
	  
}
