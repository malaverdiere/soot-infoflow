package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

/**
 * Tests that require the taint tracking engine to correctly evaluate the
 * semantics of primitive operations.
 * 
 * @author Steven Arzt
 */
public class OperationSemanticTests extends JUnitTests{

	@Test
    public void baseTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OperationSemanticTestCode: void baseTestCode()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
    }
	    
	@Test
    public void mathTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OperationSemanticTestCode: void mathTestCode()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		negativeCheckInfoflow(infoflow);
    }

}
