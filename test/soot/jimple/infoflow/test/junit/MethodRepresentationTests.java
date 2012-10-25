package soot.jimple.infoflow.test.junit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;

public class MethodRepresentationTests {

	@Test
	public void testParser(){
		String s = "<soot.jimple.infoflow.test.TestNoMain: java.lang.String function1()>";
		
		SootMethodRepresentationParser parser = new SootMethodRepresentationParser();
		SootMethodAndClass result = parser.parseSootMethodString(s);
		
		assertEquals("soot.jimple.infoflow.test.TestNoMain", result.getClassString());
		assertEquals("function1", result.getSootMethod().getName());
		assertEquals("java.lang.String", result.getSootMethod().getReturnType().toString());
	}
	
}
