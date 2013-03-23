package soot.jimple.infoflow;

import java.util.List;

import soot.jimple.infoflow.AbstractInfoflowProblem.PathTrackingMethod;
import soot.jimple.infoflow.entryPointCreators.IEntryPointCreator;
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
	 * Sets whether the information flow analysis shall stop after the first
	 * flow has been found
	 * @param stopAfterFirstFlow True if the analysis shall stop after the
	 * first flow has been found, otherwise false.
	 */
	public void setStopAfterFirstFlow(boolean stopAfterFirstFlow);


	/**
	 * Computes the information flow on a list of entry point methods. This list
	 * is used to construct an artificial main method following the Android
	 * life cycle for all methods that are detected to be part of Android's
	 * application infrastructure (e.g. android.app.Activity.onCreate)
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPointCreator the entry point creator to use for generating the dummy
	 * main method
	 * @param entryPoints the entryPoints (string conforms to SootMethod representation)
	 * @param sources list of source class+method (as string conforms to SootMethod representation)
	 * @param sinks list of sink class+method (as string conforms to SootMethod representation)
	 */
	public void computeInfoflow(String path, IEntryPointCreator entryPointCreator,
			List<String> entryPoints, List<String> sources, List<String> sinks);
	
	/**
	 * Computes the information flow on a list of entry point methods. This list
	 * is used to construct an artificial main method following the Android
	 * life cycle for all methods that are detected to be part of Android's
	 * application infrastructure (e.g. android.app.Activity.onCreate)
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPoints the entryPoints (string conforms to SootMethod representation)
	 * @param sources list of source class+method (as string conforms to SootMethod representation)
	 * @param sinks list of sink class+method (as string conforms to SootMethod representation)
	 */
	public void computeInfoflow(String path, List<String> entryPoints, List<String> sources,
			List<String> sinks);

	/**
	 * Computes the information flow on a single method. This method is
	 * directly taken as the entry point into the program, even if it is an
	 * instance method.
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPoint the main method to analyze
	 * @param sources list of source class+method (as string conforms to SootMethod representation)
	 * @param sinks list of sink class+method (as string conforms to SootMethod representation)
	 */
	public void computeInfoflow(String path, String entryPoint, List<String> sources, List<String> sinks);

	/**
	 * Computes the information flow on a list of entry point methods. This list
	 * is used to construct an artificial main method following the Android
	 * life cycle for all methods that are detected to be part of Android's
	 * application infrastructure (e.g. android.app.Activity.onCreate)
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPointCreator the entry point creator to use for generating the dummy
	 * main method
	 * @param entryPoints the entryPoints (string conforms to SootMethod representation)
	 * @param sourcesSinks manager class for identifying sources and sinks in the source code
	 */
	public void computeInfoflow(String path, IEntryPointCreator entryPointCreator,
			List<String> entryPoints, SourceSinkManager sourcesSinks);

	/**
	 * Computes the information flow on a single method. This method is
	 * directly taken as the entry point into the program, even if it is an
	 * instance method.
	 * @param path the path to the main folder of the (unpacked) class files
	 * @param entryPoint the main method to analyze
	 * @param sourcesSinks manager class for identifying sources and sinks in the source code
	 */
	public void computeInfoflow(String path, String entryPoint, SourceSinkManager sourcesSinks);

	public InfoflowResults getResults();
	
	public boolean isResultAvailable();
	
}
