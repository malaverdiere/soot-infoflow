package soot.jimple.infoflow.source;

import java.util.List;

import soot.SootMethod;

public class DefaultSourceManager extends SourceManager {

	private List<String> sources;
	
	public void setSource(List<String> newList){
		sources = newList;
	}
	
	public DefaultSourceManager(List<String> list){
		sources = list;
	}
	
	@Override
	public boolean isSourceMethod(SootMethod sMethod) {
		if(sources == null)
			return false;
		//TODO: more efficient?
		if(sources.contains(sMethod.toString())){
			return true;
		}
		return false;
	}

}
