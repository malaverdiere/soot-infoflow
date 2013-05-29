package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class OtherTests extends JUnitTests{
	
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
	
	@Test
	public void thirdCallBackTest(){
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.CallbackTestCode: void tryNext2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
	}

	@Test
	public void secondCallBackTest(){
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.CallbackTestCode: void tryNext()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
	}
	
	@Test
	public void callbackTest(){
		Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.CallbackTestCode: void checkLocation()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
	}
	
	@Test
    public void inheritanceTestSpecial(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.InheritanceTestCode: void testInheritance2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }

	@Test
    public void fieldTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void testWithField()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
	
	@Test
    public void staticInheritanceTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void testWithStaticInheritance()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
	
	@Test
    public void fieldInheritanceTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void testWithFieldInheritance()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
	
	@Test
    public void defaultlifecycleTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.utilclasses.D1static: boolean start()>");
    	epoints.add("<soot.jimple.infoflow.test.utilclasses.D1static: boolean taintIt()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
	
	@Test
    public void defaultlifecycle2Test(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.InheritanceTestCode: void testInheritance1()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
	
    @Test
    public void staticTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void staticTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
	}
    
    @Test
    public void static2Test(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void static2Test()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
    
    @Test
    public void ConstructorFinalClassTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void genericsfinalconstructorProblem()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
    
    @Test
    public void ptsTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.PTSTestCode: void testPointsToSet()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
    
    @Test
    public void negativeTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.EasyNegativeTestCode: void easyNegativeTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);	
    }
    
    @Test
    public void mailTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MailTest: void methodTainted()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
    
    @Test
    public void mailNegativeTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MailTest: void methodNotTainted()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
    
    @Test
    public void mail2Test(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MailTest: void method2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
    
    @Test
    public void mail2TestNegative(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MailTest: void method2NotTainted()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
    
    @Test
    public void mail3Test(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MailTest: void method3()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
    
    @Test
    public void mail3NegativeTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MailTest: void method3NotTainted()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
    
    @Test
    public void mail4(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MailTest: void method4()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
    
    @Test
    public void mail5(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MailTest: void method5()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
    
    @Test
    public void mail6(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.MailTest: void method6()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
    
   @Test
    public void stringConcatTest(){
	   Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void stringConcatTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }
   
   @Test
   public void stringConcatTestSmall(){
	   Infoflow infoflow = initInfoflow();
   		List<String> epoints = new ArrayList<String>();
   		epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void stringConcatTestSmall()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
   }
   
   @Test
   public void stringConcatTestSmall2(){
	   Infoflow infoflow = initInfoflow();
   		List<String> epoints = new ArrayList<String>();
   		epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void stringConcatTestSmall2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
   }
    
   @Test
   public void stringConcatTestSmall3(){
	   Infoflow infoflow = initInfoflow();
   		List<String> epoints = new ArrayList<String>();
   		epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void stringConcatTestSmall3()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
   }

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
    public void innerClassTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void innerClassTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }

    @Test
    public void classHierarchyTest(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void classHierarchyTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
    
    @Test
    public void classHierarchyTest2(){
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void classHierarchyTest2()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
    }

    @Test
    public void multiCallTest(){
    	taintWrapper = true;
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void multiCallTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
		Assert.assertEquals(2, infoflow.getResults().size());
    }

    @Test
    public void loopTest(){
    	taintWrapper = true;
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void loopTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
		Assert.assertEquals(1, infoflow.getResults().size());
    }

    @Test
    public void dataObjectTest(){
    	taintWrapper = true;
    	Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void dataObjectTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
		Assert.assertEquals(1, infoflow.getResults().size());
    }

}
