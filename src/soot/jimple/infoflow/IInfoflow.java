package soot.jimple.infoflow;

import java.util.List;

import soot.jimple.infoflow.data.AnalyzeClass;

public interface IInfoflow {

	/**
	 * computes the information flow
	 * @param path the path to the main folder of the unpacked class files (for example from an .apk)
	 * @param classes the entryPoint classes (including the entryPoint methods and other information)
	 * @param sources list of source class+method (as String)
	 * @param sinkslist of sink class+method (as String)
	 */
	public void computeInfoflow(String path, List<AnalyzeClass> classes, List<String> sources, List<String> sinks);
	
}
