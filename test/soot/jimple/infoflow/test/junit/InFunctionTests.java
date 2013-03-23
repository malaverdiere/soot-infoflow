package soot.jimple.infoflow.test.junit;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import soot.jimple.infoflow.AbstractInfoflowProblem.PathTrackingMethod;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.Infoflow;

public class InFunctionTests extends JUnitTests {

	private static final String SOURCE_STRING_PARAMETER = "@parameter0: java.lang.String";
	private static final String SINK_STRING_RETURN = "secret";
	
	@Override
	protected Infoflow initInfoflow() {
		Infoflow infoflow = super.initInfoflow();
    	return infoflow;
	}

    @Test
    public void inFunctionTest1(){
    	Infoflow infoflow = initInfoflow();
    	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
    	String epoint = "<soot.jimple.infoflow.test.InFunctionCode: java.lang.String infSourceCode1(java.lang.String)>";

    	DefaultSourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks);
    	ssm.setParameterTaintMethods(Collections.singletonList(epoint));
    	ssm.setReturnTaintMethods(Collections.singletonList(epoint));
		
    	infoflow.computeInfoflow(path, epoint, ssm);
		Assert.assertTrue(infoflow.getResults().isPathBetween(SINK_STRING_RETURN, SOURCE_STRING_PARAMETER));
    }

    @Test
    public void inFunctionTest2(){
    	Infoflow infoflow = initInfoflow();
    	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
    	String epoint = "<soot.jimple.infoflow.test.InFunctionCode: java.lang.String infSourceCode2(java.lang.String)>";

    	DefaultSourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks);
    	ssm.setParameterTaintMethods(Collections.singletonList(epoint));
    	ssm.setReturnTaintMethods(Collections.singletonList(epoint));
		
    	infoflow.computeInfoflow(path, epoint, ssm);
		Assert.assertTrue(infoflow.getResults().getResults().isEmpty());
    }

    @Test
    public void inFunctionTest3(){
    	Infoflow infoflow = initInfoflow();
    	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
    	String epoint = "<soot.jimple.infoflow.test.InFunctionCode: java.lang.String infSourceCode3(java.lang.String)>";

    	DefaultSourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks);
    	ssm.setParameterTaintMethods(Collections.singletonList(epoint));
    	ssm.setReturnTaintMethods(Collections.singletonList(epoint));
		
    	infoflow.computeInfoflow(path, epoint, ssm);
		Assert.assertTrue(infoflow.getResults().isPathBetween(SINK_STRING_RETURN, SOURCE_STRING_PARAMETER));
    }

}
