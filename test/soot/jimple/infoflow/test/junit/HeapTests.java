/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import soot.RefType;
import soot.Scene;
import soot.SootField;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.taintWrappers.AbstractTaintWrapper;
import soot.jimple.infoflow.test.utilclasses.TestWrapper;
/**
 * tests aliasing of heap references
 */
public class HeapTests extends JUnitTests {
	
	@Test(timeout=300000)
	public void testForEarlyTermination(){
		Infoflow infoflow = initInfoflow();
		infoflow.setTaintWrapper(new TestWrapper());
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void testForEarlyTermination()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
	}
	
	@Test(timeout=300000)
	public void testForLoop(){
		Infoflow infoflow = initInfoflow();
		infoflow.setTaintWrapper(new TestWrapper());
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void testForLoop()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		checkInfoflow(infoflow, 1);
	}
	
	
	@Test(timeout=300000)
	public void testForWrapper(){
		Infoflow infoflow = initInfoflow();
		infoflow.setTaintWrapper(new TestWrapper());
	    List<String> epoints = new ArrayList<String>();
	    epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void testForWrapper()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
	}
	
	@Test(timeout=300000)
    public void simpleTest(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void simpleTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	@Test(timeout=300000)
    public void argumentTest(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void argumentTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	@Test(timeout=300000)
    public void negativeTest(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void negativeTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	@Test(timeout=300000)
    public void doubleCallTest(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void doubleCallTest()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	@Test(timeout=300000)
    public void heapTest0(){
	  Infoflow infoflow = initInfoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void methodTest0()>");
		infoflow.computeInfoflow(path, epoints,sources, sinks);
		negativeCheckInfoflow(infoflow);
    }
	
	  @Test(timeout=300000)
	    public void heapTest1(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void methodTest1()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
	    }
	  
	  @Test(timeout=300000)
	    public void testExample1(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.ForwardBackwardTest: void testMethod()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
	    }
	  
	    @Test(timeout=300000)
	    public void testReturn(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void methodReturn()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
	    }
	    
	    @Test(timeout=300000)
	    public void testTwoLevels(){
		  Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void twoLevelTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
	    }
	    
	    @Test(timeout=300000)
	    public void multiAliasTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void multiAliasTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void overwriteAliasTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void overwriteAliasTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
			Assert.assertEquals(0, infoflow.getResults().size());
	    }
	    
	    @Test(timeout=300000)
	    public void arrayAliasTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void arrayAliasTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void functionAliasTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void functionAliasTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void functionAliasTest2(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void functionAliasTest2()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void multiLevelTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void multiLevelTaint()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void multiLevelTest2(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void multiLevelTaint2()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void negativeMultiLevelTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void negativeMultiLevelTaint()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
	    }

	    @Test(timeout=300000)
	    public void negativeMultiLevelTest2(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void negativeMultiLevelTaint2()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
	    }

	    @Test(timeout=300000)
	    public void threeLevelTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void threeLevelTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void threeLevelShortAPTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	
	    	int oldAPLength = Infoflow.getAccessPathLength();
	    	infoflow.setAccessPathLength(1);

	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void threeLevelTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			
			infoflow.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void recursionTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void recursionTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void activationUnitTest1(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void activationUnitTest1()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void activationUnitTest2(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void activationUnitTest2()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
			Assert.assertEquals(0, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void activationUnitTest3(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void activationUnitTest3()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void activationUnitTest4(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void activationUnitTest4()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
			Assert.assertEquals(0, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void activationUnitTest4b(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void activationUnitTest4b()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
			Assert.assertEquals(0, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void activationUnitTest5(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void activationUnitTest5()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);
			Assert.assertEquals(0, infoflow.getResults().size());
	    }

	    @Test(timeout=300000)
	    public void returnAliasTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void returnAliasTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }
	    
	    @Test(timeout=300000)
	    public void callPerformanceTest(){
	    	taintWrapper = false;
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSinks(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void callPerformanceTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }
	    
	    @Test(timeout=300000)
	    public void aliasesTest(){
	    	taintWrapper = false;
	    	
	    	Infoflow infoflow = initInfoflow();
	    	int oldLength = Infoflow.getAccessPathLength();
	    	infoflow.setAccessPathLength(3);

	    	infoflow.setInspectSources(false);
	    	infoflow.setInspectSinks(false);
	    	infoflow.setEnableImplicitFlows(false);
	    	
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void testAliases()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());

			infoflow.setAccessPathLength(oldLength);
	    }

	    @Test(timeout=300000)
	    public void wrapperAliasesTest(){
	    	taintWrapper = false;
	    	
	    	Infoflow infoflow = initInfoflow();
	    	int oldLength = Infoflow.getAccessPathLength();
	    	infoflow.setAccessPathLength(3);
	    	
	    	infoflow.setTaintWrapper(new AbstractTaintWrapper() {
				
				@Override
				public boolean isExclusiveInternal(Stmt stmt, AccessPath taintedPath) {
					return stmt.containsInvokeExpr()
							&& (stmt.getInvokeExpr().getMethod().getName().equals("foo2")
									|| stmt.getInvokeExpr().getMethod().getName().equals("bar2"));
				}
				
				@Override
				public Set<AccessPath> getTaintsForMethod(Stmt stmt, AccessPath taintedPath) {
					if (!stmt.containsInvokeExpr())
						return Collections.singleton(taintedPath);
					
					Set<AccessPath> res = new HashSet<AccessPath>();
					res.add(taintedPath);

					// We use a path length of 1, i.e. do not work with member fields,
					// hence the commented-out code
					if (stmt.getInvokeExpr().getMethod().getName().equals("foo2")) {
						InstanceInvokeExpr iinv = (InstanceInvokeExpr) stmt.getInvokeExpr();
						if (taintedPath.getPlainLocal() == iinv.getArg(0)) {
							RefType rt = (RefType) iinv.getBase().getType();
							AccessPath ap = new AccessPath(iinv.getBase(), new SootField[] { rt.getSootClass().getFieldByName("b1")/*,
								Scene.v().getSootClass("soot.jimple.infoflow.test.HeapTestCode$B").getFieldByName("attr"),
								Scene.v().getSootClass("soot.jimple.infoflow.test.HeapTestCode$A").getFieldByName("b")*/ });
							res.add(ap);
						}
						if (taintedPath.getPlainLocal() == iinv.getArg(1)) {
							RefType rt = (RefType) iinv.getBase().getType();
							AccessPath ap = new AccessPath(iinv.getBase(), new SootField[] { rt.getSootClass().getFieldByName("b2")/*,
								Scene.v().getSootClass("soot.jimple.infoflow.test.HeapTestCode$B").getFieldByName("attr"),
								Scene.v().getSootClass("soot.jimple.infoflow.test.HeapTestCode$A").getFieldByName("b")*/ });
							res.add(ap);
						}
					}
					else if (stmt.getInvokeExpr().getMethod().getName().equals("bar2")) {
						InstanceInvokeExpr iinv = (InstanceInvokeExpr) stmt.getInvokeExpr();
						if (taintedPath.getPlainLocal() == iinv.getArg(0)) {	
							RefType rt = (RefType) iinv.getBase().getType();
							AccessPath ap = new AccessPath(iinv.getBase(), new SootField[] { rt.getSootClass().getFieldByName("b1")/*,
									Scene.v().getSootClass("soot.jimple.infoflow.test.HeapTestCode$B").getFieldByName("attr"),
									Scene.v().getSootClass("soot.jimple.infoflow.test.HeapTestCode$A").getFieldByName("b")*/} );
							res.add(ap);
						}
						else if (taintedPath.getPlainValue() == iinv.getBase()
								/*
								&& taintedPath.getFirstField().getName().equals("b2")
								// .attr
								&& taintedPath.getLastField().getName().equals("b")*/) {
							DefinitionStmt def = (DefinitionStmt) stmt;
							AccessPath ap = new AccessPath(def.getLeftOp(),
									Scene.v().getSootClass("soot.jimple.infoflow.test.HeapTestCode$A").getFieldByName("b"));
							res.add(ap);
						}
					}
					
					return res;
				}
			});

	    	infoflow.setInspectSources(false);
	    	infoflow.setInspectSinks(false);
	    	infoflow.setEnableImplicitFlows(false);
	    	
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void testWrapperAliases()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());

			infoflow.setAccessPathLength(oldLength);
	    }

	    @Test(timeout=300000)
	    public void negativeAliasesTest(){
	    	taintWrapper = false;
	    	
	    	Infoflow infoflow = initInfoflow();
	    	int oldLength = Infoflow.getAccessPathLength();
	    	infoflow.setAccessPathLength(3);

	    	infoflow.setInspectSources(false);
	    	infoflow.setInspectSinks(false);
	    	infoflow.setEnableImplicitFlows(false);
	    	
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void negativeTestAliases()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			negativeCheckInfoflow(infoflow);

			infoflow.setAccessPathLength(oldLength);
	    }

	    @Test(timeout=300000)
	    public void aliasPerformanceTest(){
	    	taintWrapper = false;
	    	
	    	Infoflow infoflow = initInfoflow();
	    	int oldLength = Infoflow.getAccessPathLength();
	    	infoflow.setAccessPathLength(3);

	    	infoflow.setInspectSources(false);
	    	infoflow.setInspectSinks(false);
	    	infoflow.setEnableImplicitFlows(false);
	    	
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void aliasPerformanceTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 2);
			Assert.assertEquals(2, infoflow.getResults().size());

			infoflow.setAccessPathLength(oldLength);
	    }

	    @Test(timeout=300000)
	    public void aliasPerformanceTestFIS(){
	    	taintWrapper = false;
	    	
	    	Infoflow infoflow = initInfoflow();
	    	int oldLength = Infoflow.getAccessPathLength();
	    	infoflow.setAccessPathLength(3);

	    	infoflow.setInspectSources(false);
	    	infoflow.setInspectSinks(false);
	    	infoflow.setEnableImplicitFlows(false);
	    	infoflow.setFlowSensitiveAliasing(false);
	    	
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void aliasPerformanceTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 3);		// we're not flow sensitive, so we get a spurious one
			Assert.assertEquals(3, infoflow.getResults().size());

			infoflow.setAccessPathLength(oldLength);
	    }

	    @Test(timeout=300000)
	    public void backwardsParameterTest(){
	    	taintWrapper = false;
	    	
	    	Infoflow infoflow = initInfoflow();
	    	infoflow.setInspectSources(false);
	    	infoflow.setInspectSinks(false);
	    	infoflow.setEnableImplicitFlows(false);
	    	
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.HeapTestCode: void backwardsParameterTest()>");
			infoflow.computeInfoflow(path, epoints,sources, sinks);
			checkInfoflow(infoflow, 1);
			Assert.assertEquals(1, infoflow.getResults().size());
	    }

}
