package soot.jimple.infoflow.source;
import java.util.ArrayList;
import java.util.List;

import soot.jimple.infoflow.data.Source;



public class SourceListParser {
	
	static List<Source> parse(List<String> sourceList){
		List<Source> result = new ArrayList<Source>();
		
		for(String sourceString : sourceList){
			if(sourceString.contains(".")){
				String classString = sourceString.substring(0, sourceString.lastIndexOf('.'));
				String methodName = sourceString.substring(sourceString.lastIndexOf('.')+1);
				if(methodName.contains("(")){
					//FIXME: not precise enough? ignores params...
					methodName = methodName.substring(0,methodName.indexOf('('));
				}
				result.add(new Source(classString, methodName));
			}
		}
		
		return result;
	}
}
