package soot.jimple.infoflow.source;

import java.util.List;

import soot.jimple.infoflow.data.Source;

public class DefaultSourceManager extends SourceManager {

	private List<Source> sources;
	
	public void setSource(List<Source> newList){
		sources = newList;
	}
	
	public DefaultSourceManager(List<Source> list){
		sources = list;
	}
	
	@Override
	public boolean isSourceMethod(Class<?> methodClass, String methodName) {
		if(sources == null)
			return false;
		//TODO: more efficient?
		for(Source s : sources){
			if(s.getMethod().equals(methodName)){ //TODO: tolowerCase?
				if(s.getTheClass().equals(methodClass.getSimpleName()))
					return true;
				for(Class<?> cl : methodClass.getClasses()){
					if(s.getTheClass().equals(cl.getSimpleName()))
						return true;
				}
			}
		}
		return false;
	}

}
