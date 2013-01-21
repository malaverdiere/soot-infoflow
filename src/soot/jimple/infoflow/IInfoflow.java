package soot.jimple.infoflow;

import java.util.HashMap;
import java.util.List;

import soot.jimple.infoflow.source.SourceSinkManager;

public interface IInfoflow {

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
	 * @param entryPoints the entryPoints (string conforms to SootMethod representation)
	 * @param sourcesSinks manager class for identifying sources andsinks in the source code
	 */
	public void computeInfoflow(String path, List<String> entryPoints, SourceSinkManager sourcesSinks);
	
	public HashMap<String, List<String>> getResults();
	
	public boolean isResultAvailable();
	
	/**
	 * default: not Local
	 * TODO: rename (not local...)
	 * @param local
	 */
	public void setLocalInfoflow(boolean local);
}
