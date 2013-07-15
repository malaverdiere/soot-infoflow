package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
/**
 * test taint propagation of fields. A certain length of the access paths is required to distinguish the tainted fields.
 */
public class LengthTests extends JUnitTests {

	@Test
	public void easyLengthTest(){
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.LengthTestCode: void easy1()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
	}
	
	@Test
	public void lengthTest(){
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.LengthTestCode: void method1()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
	}
	
	@Test
	public void lengthTest2(){
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.LengthTestCode: void method2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
	}
}
