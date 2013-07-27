package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;
/**
 * contain test cases for taint propagation in Maps.
 */
public class MapTests extends JUnitTests {

    @Test
    public void mapPos0Test(){
    	Infoflow infoflow = initInfoflow();

    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void writeReadPos0Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);		
		
		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }
    
    @Test
    public void mapPos1Test(){
    	Infoflow infoflow = initInfoflow();

    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);
    	
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void writeReadPos1Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }

    @Test
    public void concreteMapPos0Test(){
    	Infoflow infoflow = initInfoflow();

    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);
    	
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteReadPos0Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }
    
    @Test
    public void concreteLinkedMapPos0Test(){
    	Infoflow infoflow = initInfoflow();

    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteLinkedWriteReadPos0Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }

    @Test
    public void concreteMapPos1Test(){
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteReadPos1Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }
    
    @Test
    public void concreteMapTest2(){
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteRead2Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    
		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }
    
    @Test
    public void mapIteratorTest(){
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void iteratorTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);		

		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }
    
    @Test
    public void mapEntryTest(){
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void entryTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);	
    
		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }
    
    @Test
    public void concreteTableTest(){
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteReadTableTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
		
		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }
    
    @Test
    public void concreteNegativeTest(){
    	Infoflow infoflow = initInfoflow();
    	
    	int oldAPLength = Infoflow.getAccessPathLength();
    	infoflow.setAccessPathLength(1);

    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MapTestCode: void concreteWriteReadNegativeTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    
		infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    }

}
