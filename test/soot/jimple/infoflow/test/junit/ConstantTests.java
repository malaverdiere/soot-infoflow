package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
/**
 * ensure proper taint propagation for constant values. 
 */
public class ConstantTests extends JUnitTests {
	
	  @Test
	    public void easyConstantFieldTest(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ConstantTestCode: void easyConstantFieldTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);	
	    }
	  
	  @Test
	    public void easyConstantVarTest(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ConstantTestCode: void easyConstantVarTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);	
	    }
	  
	  @Test
	    public void constantFieldArrayTest(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ConstantTestCode: void constantFieldArrayTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 2);	
	    }
	  
	  @Test
	    public void constantFieldTest(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ConstantTestCode: void constantFieldTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);	
	    }

}
