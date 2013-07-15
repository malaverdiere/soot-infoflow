package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
/**
 * covers taint propagation of fields with classes from a class hierarchy
 */
public class HierarchyTests extends JUnitTests {

	   @Test
	    public void hierarchytaintedTest(){
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HierarchyTestCode: void taintedOutputTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
	    }
	    
	    @Test
	    public void hierarchyuntaintedTest(){
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HierarchyTestCode: void untaintedOutputTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
	    }
	    
	    @Test
	    public void classHierarchyTest(){
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HierarchyTestCode: void classHierarchyTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
	    }
	    
	    @Test
	    public void classHierarchyTest2(){
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HierarchyTestCode: void classHierarchyTest2()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
	    }
}
