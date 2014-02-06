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
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

/**
 * These tests check taint propagation over various types
 * 
 * @author Steven Arzt
 */
public class TypeTests extends JUnitTests {

	@Test(timeout=300000)
	public void stringToObjectTest(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void typeTest1()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}
	
	@Test(timeout=300000)
	public void classCastTest1(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void classCastTest1()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void classCastTest2(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void classCastTest2()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void classCastTest3(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void classCastTest3()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void instanceofTest1(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void instanceofTest1()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 2);	
	}

	@Test(timeout=300000)
	public void callTargetTest1(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void callTargetTest1()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void arrayObjectCastTest(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void arrayObjectCastTest()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void arrayObjectCastTest2(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void arrayObjectCastTest2()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void callTypeTest(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void callTypeTest()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void callTypeTest2(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void callTypeTest2()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void arrayCastAndAliasTest(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void arrayCastAndAliasTest()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void arrayCastAndAliasTest2(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void arrayCastAndAliasTest2()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

	@Test(timeout=300000)
	public void arrayIncompatibleCastAndAliasTest(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void arrayIncompatibleCastAndAliasTest()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);		// doesn't really matter since the code cannot be executed anyway	
	}

	@Test(timeout=300000)
	public void fieldIncompatibleCastAndAliasTest(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void fieldIncompatibleCastAndAliasTest()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		negativeCheckInfoflow(infoflow);	
	}

	@Test(timeout=300000)
	public void twoDimensionArrayTest(){
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.TypeTestCode: void twoDimensionArrayTest()>");
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);	
	}

}
