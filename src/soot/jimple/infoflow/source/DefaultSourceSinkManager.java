package soot.jimple.infoflow.source;

import java.util.List;

import soot.SootMethod;

public class DefaultSourceSinkManager extends MethodBasedSourceSinkManager {

	private List<String> sources;
	private List<String> sinks;
	
	public void setSources(List<String> sources){
		this.sources = sources;
	}
	
	public void setSinks(List<String> sinks){
		this.sinks = sinks;
	}

	public DefaultSourceSinkManager(List<String> sources, List<String> sinks){
		this.sources = sources;
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
