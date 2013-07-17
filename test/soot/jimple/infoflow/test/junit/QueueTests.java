package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.Ignore;

import soot.jimple.infoflow.Infoflow;
/**
 * test taint propagation in queues
 */
@Ignore
public class QueueTests extends JUnitTests {

	@Test
	public void concreteSynchronousQueueTest() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.QueueTestCode: void concreteWriteReadTest()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);
	}

	@Test
	public void concreteSynchronousQueueNegativeTest() {
		Infoflow infoflow = initInfoflow();
		List<String> epoints = new ArrayList<String>();
		epoints.add("<soot.jimple.infoflow.test.QueueTestCode: void concreteWriteReadNegativeTest()>");
		infoflow.computeInfoflow(path, epoints, sources, sinks);
		negativeCheckInfoflow(infoflow);
	}

}
