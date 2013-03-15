package soot.jimple.infoflow.source;

import java.util.List;

import soot.SootMethod;

/**
 * A {@link SourceSinkManager} working on lists of source and sink methods
 * 
 * @author Steven Arzt
 */
public class DefaultSourceSinkManager extends MethodBasedSourceSinkManager {

	private List<String> sources;
	private List<String> sinks;
	
	/**
	 * Creates a new instance of the {@link DefaultSourceSinkManager} class
	 * @param sources The list of methods to be treated as sources
	 * @param sinks The list of methods to be treated as sins
	 */
	public DefaultSourceSinkManager(List<String> sources, List<String> sinks){
		this.sources = sources;
		this.sinks = sinks;
	}

	/**
	 * Sets the list of methods to be treated as sources
	 * @param sources The list of methods to be treated as sources
	 */
	public void setSources(List<String> sources){
		this.sources = sources;
	}
	
	/**
	 * Sets the list of methods to be treated as sinks
	 * @param sources The list of methods to be treated as sinks
	 */
	public void setSinks(List<String> sinks){
		this.sinks = sinks;
	}
	
	@Override
	public boolean isSourceMethod(SootMethod sMethod) {
		return sources.contains(sMethod.toString());
	}

	@Override
	public boolean isSinkMethod(SootMethod sMethod) {
		return sinks.contains(sMethod.toString());
	}

}
