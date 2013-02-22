package soot.jimple.infoflow;

import java.util.List;

import soot.jimple.infoflow.AbstractInfoflowProblem.PathTrackingMethod;
import soot.jimple.infoflow.source.SourceSinkManager;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;

public interface IInfoflow {
	
	/**
	 * Sets the taint wrapper for deciding on taint propagation through black-box
	 * methods
	 * @param wrapper The taint wrapper object that decides on how information is
	 * propagated through black-box methods
	 */
	public void setTaintWrapper(ITaintPropagationWrapper wrapper);

	/**
	 * Sets whether and how the paths between the sources and sinks shall be
	 * tracked
	 * @param method The method for tracking data flow paths through the
	 * program.
	 */
	public void setPathTracking(PathTrackingMethod method);

	/**
	 * Sets whether the parameters of the entry point methods shall be considered
	 * as sources
	 * @param computeParamFlows True if entry point parameters are sources,
	 * otherwise false
	 */
	public void setComputeParamFlows(boolean computeParamFlows);
	
	/**
	 * Sets whether the return statements of the entry point methods shall be
	 * considered as sinks
	 * @param returnIsSink True if entry point return values are sinks,
	 * otherwise false
	 */
	public void setReturnIsSink(boolean returnIsSink);

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
