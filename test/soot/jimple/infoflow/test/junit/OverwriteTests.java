package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class OverwriteTests extends JUnitTests {

	  @Test
	    public void varOverwriteTest(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.OverwriteTestCode: void varOverwrite()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);	
	    }
	  
	  @Test
	    public void staticFieldOverwriteTest(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.OverwriteTestCode: void staticFieldOverwrite()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);		
	    }
	  
	  @Test
	    public void fieldOverwriteTest(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.OverwriteTestCode: void fieldOverwrite()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);		
	    }
	  
	  @Test
	    public void returnOverwriteTest(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.OverwriteTestCode: void returnOverwrite()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);		
	    }
	  
	  @Test
	    public void returnOverwriteTest2(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.OverwriteTestCode: void returnOverwrite2()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);		
	    }
	  
	  @Test
	    public void returnOverwriteTest3(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.OverwriteTestCode: void returnOverwrite3()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);		
	    }
	    
	  @Test
	    public void returnOverwriteTest4(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.OverwriteTestCode: void returnOverwrite4()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);		
	    }

}
