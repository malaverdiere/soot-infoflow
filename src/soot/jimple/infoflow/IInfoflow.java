package soot.jimple.infoflow;

import java.util.List;

public interface IInfoflow {

	/**
	 * computes the information flow
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPoints the entryPoints (string conforms to SootMethod representation)
	 * @param sources list of source class+method (as string conforms to SootMethod representation)
	 * @param sinks list of sink class+method (as string conforms to SootMethod representation)
	 */
	public void computeInfoflow(String path, List<String> entryPoints, List<String> sources, List<String> sinks);
	
}
