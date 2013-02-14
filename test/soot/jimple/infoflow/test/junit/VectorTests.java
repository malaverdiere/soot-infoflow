package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class VectorTests extends JUnitTests {

	@Test
	public void vectorRWPos0Test() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void concreteWriteReadPos0Test()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
		// assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		// assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
	}

	@Test
	public void vectorRWPos1Test() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void concreteWriteReadPos1Test()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
		}

	@Test
	public void vIteratorPos0Test() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void iteratorPos0Test()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
		// assertTrue(errOutputStream.toString().contains("taintedElement2 contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		// assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
	}
	
	@Test
	public void vIteratorPos1Test() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void iteratorPos1Test()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
	}

	@Test
	public void concreteVIteratorTest() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void concreteIteratorTest()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow);
		// assertTrue(errOutputStream.toString().contains("taintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
	}

	@Test
	public void concreteNegativeTest() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.VectorTestCode: void concreteWriteReadNegativeTest()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		negativeCheckInfoflow(infoflow);
		// assertFalse(errOutputStream.toString().contains("untaintedElement contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		// assertTrue(errOutputStream.toString().contains("tainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager"));
	}

}
