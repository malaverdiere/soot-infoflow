package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.Infoflow;

public class OtherTests extends JUnitTests{


    @Test
    public void staticTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void staticTest()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		assertTrue(errOutputStream.toString().contains("alsoTainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("<soot.jimple.infoflow.test.utilclasses.ClassWithStatic: java.lang.String staticTitle> contains value from staticinvoke"));

    }
    
    @Test
    public void ConstructorFinalClassTest(){
    	Infoflow infoflow = new Infoflow();
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.OtherTestCode: void genericsfinalconstructorProblem()>");
		infoflow.computeInfoflow(path, epoints,null, null);
		
		assertTrue(errOutputStream.toString().contains("alsoTainted contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId()>()"));
		assertTrue(errOutputStream.toString().contains("this.<soot.jimple.infoflow.test.utilclasses.ClassWithFinal: java.lang.String b> contains value from staticinvoke <soot.jimple.infoflow.test.android.TelephonyManager: java.lang.String getDeviceId("));
    	
    }
}
