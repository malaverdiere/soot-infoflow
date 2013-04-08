package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import soot.jimple.infoflow.AbstractInfoflowProblem.PathTrackingMethod;
import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;

public class InFunctionTests extends JUnitTests {

	private static final String SOURCE_STRING_PARAMETER = "@parameter0: java.lang.String";
	private static final String SOURCE_STRING_PARAMETER2 = "@parameter1: java.lang.String";
	
	private static final String SOURCE_INT_PARAMETER = "@parameter0: int";
	private static final String SOURCE_INT_PARAMETER2 = "@parameter1: int";
	
	private static final String SINK_STRING_RETURN = "secret";
	private static final String SINK_STRING_RETURN_R5 = "$r5";
	
	@Override
	protected Infoflow initInfoflow() {
		taintWrapper = true;
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

    @Test
    public void inFunctionTest4(){
    	Infoflow infoflow = initInfoflow();
    	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
    	List<String> epoint = new ArrayList<String>();
    	epoint.add("<soot.jimple.infoflow.test.InFunctionCode: void setTmp(java.lang.String)>");
    	epoint.add("<soot.jimple.infoflow.test.InFunctionCode: java.lang.String foo(java.lang.String,java.lang.String)>");

    	DefaultSourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks);
    	ssm.setParameterTaintMethods(epoint);
    	ssm.setReturnTaintMethods(epoint);
		
    	infoflow.computeInfoflow(path, new DefaultEntryPointCreator(), epoint, ssm);
		Assert.assertTrue(infoflow.getResults().isPathBetween(SINK_STRING_RETURN_R5, SOURCE_STRING_PARAMETER));
		Assert.assertTrue(infoflow.getResults().isPathBetween(SINK_STRING_RETURN_R5, SOURCE_STRING_PARAMETER2));
    }

    @Test
    public void parameterFlowTest(){
    	Infoflow infoflow = initInfoflow();
    	infoflow.setPathTracking(PathTrackingMethod.ForwardTracking);
    	List<String> epoint = new ArrayList<String>();
    	epoint.add("<soot.jimple.infoflow.test.InFunctionCode: int paraToParaFlow(int,int,"
    			+ "soot.jimple.infoflow.test.InFunctionCode$DataClass,"
    			+ "soot.jimple.infoflow.test.InFunctionCode$DataClass)>");

    	DefaultSourceSinkManager ssm = new DefaultSourceSinkManager(sources, sinks);
    	ssm.setParameterTaintMethods(epoint);
    	ssm.setReturnTaintMethods(epoint);
    	
    	infoflow.computeInfoflow(path, new DefaultEntryPointCreator(), epoint, ssm);
    	Assert.assertNotNull(infoflow.getResults());
		Assert.assertTrue(infoflow.getResults().isPathBetween("b", SOURCE_INT_PARAMETER2));
		Assert.assertFalse(infoflow.getResults().isPathBetween("b", SOURCE_INT_PARAMETER));
    }

}
