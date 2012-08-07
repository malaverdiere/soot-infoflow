package soot.jimple.infoflow;

import java.util.List;

import soot.jimple.infoflow.data.AnalyzeClass;

public interface IInfoflow {

	/**
	 * 
	 * @param path
	 * @param classes
	 * @param sources
	 * @param sinks
	 */
	public void computeInfoflow(String path, List<AnalyzeClass> classes, List<String> sources, List<String> sinks);
	
	
	public void computeInfoflow(String classNameWithPath, boolean hasMainMethod, List<String> entryMethodNames);
}
