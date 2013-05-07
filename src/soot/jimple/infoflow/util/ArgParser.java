package soot.jimple.infoflow.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgParser {
	public static String METHODKEYWORD = "-entrypoints";
	public static String SOURCEKEYWORD = "-sources";
	public static String SINKKEYWORD = "-sinks";
	public static String PATHKEYWORD = "-path";
	
	public List<List<String>> parseClassArguments(String[] args){
		List<String> argList = Arrays.asList(args);
		List<String> ePointList;
		List<String> sourceList = new ArrayList<String>();
		List<String> sinkList = new ArrayList<String>();
		List<String> pathList = new ArrayList<String>();
	
		if(argList.contains(METHODKEYWORD)){
			ePointList = getListToAttribute(argList, METHODKEYWORD);
		} else{
			System.err.println("parameter '"+ METHODKEYWORD+ "' is missing or has not enough arguments!");
			return null;
		}
		if(argList.contains(SOURCEKEYWORD)){
			sourceList = getListToAttribute(argList, SOURCEKEYWORD);
		}
		if(argList.contains(SINKKEYWORD)){
			sinkList = getListToAttribute(argList, SINKKEYWORD);
		}
			
		if(argList.contains(PATHKEYWORD)){
			pathList = getListToAttribute(argList, PATHKEYWORD);
		}
		
		 List<List<String>> resultlist = new ArrayList<List<String>>();
		 resultlist.add(ePointList);
		 resultlist.add(sourceList);
		 resultlist.add(sinkList);
		 resultlist.add(pathList);
		
		return resultlist;
		
	}

	private List<String> getListToAttribute(List<String> argList, String attr){
		List<String> result = new ArrayList<String>();
		if(argList.indexOf(attr)+1 < argList.size() && !argList.get(argList.indexOf(attr)+1).startsWith("-")){
			int position = argList.indexOf(attr);
			while(position +1 < argList.size() && !argList.get(position+1).startsWith("-")){
				result.add(argList.get(position+1));
				position++;
			}
		}
		
		return result;
	}

}
