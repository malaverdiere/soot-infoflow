package soot.jimple.infoflow;

import java.util.List;

import soot.jimple.infoflow.source.SourceSinkManager;
import soot.jimple.infoflow.util.ITaintPropagationWrapper;

public interface IInfoflow {
	
	public void setTaintWrapper(ITaintPropagationWrapper wrapper);

	/**
	 * computes the information flow
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPoints the entryPoints (string conforms to SootMethod representation)
	 * @param sources list of source class+method (as string conforms to SootMethod representation)
	 * @param sinks list of sink class+method (as string conforms to SootMethod representation)
	 */
	public void computeInfoflow(String path, List<String> entryPoints, List<String> sources, List<String> sinks);
	
	/**
	 * computes the information flow
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPoint the main method to analyze
	 * @param sources list of source class+method (as string conforms to SootMethod representation)
	 * @param sinks list of sink class+method (as string conforms to SootMethod representation)
	 */
	public void computeInfoflow(String path, String entryPoint, List<String> sources, List<String> sinks);

	/**
	 * computes the information flow
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPoints the entryPoints (string conforms to SootMethod representation)
	 * @param sourcesSinks manager class for identifying sources and sinks in the source code
	 */
	public void computeInfoflow(String path, List<String> entryPoints, SourceSinkManager sourcesSinks);
	
	/**
	 * computes the information flow
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPoint the main method to analyze
	 * @param sourcesSinks manager class for identifying sources and sinks in the source code
	 */
	public void computeInfoflow(String path, String entryPoint, SourceSinkManager sourcesSinks);

	public InfoflowResults getResults();
	
	public boolean isResultAvailable();
	
}
