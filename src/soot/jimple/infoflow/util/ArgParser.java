package soot.jimple.infoflow.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import soot.jimple.infoflow.data.AnalyzeClass;
import soot.jimple.infoflow.data.AnalyzeMethod;

public class ArgParser {
	public static String CLASSKEYWORD = "-class";
	public static String METHODKEYWORD = "-methods";
	public static String ANDROIDKEYWORD = "-android";
	public static String MAINKEYWORD = "-nomain";
	
	public AnalyzeClass parseClassArguments(String[] args){
		AnalyzeClass aClass = new AnalyzeClass();
		List<String> argList = Arrays.asList(args);
		if(argList.contains(CLASSKEYWORD) && argList.indexOf(CLASSKEYWORD)+1 < argList.size() && !argList.get(argList.indexOf(CLASSKEYWORD)+1).startsWith("-")){
			aClass.setNameWithPath(argList.get(argList.indexOf(CLASSKEYWORD)+1));
			if(argList.contains(METHODKEYWORD)){ 
				List<AnalyzeMethod> methodList = new ArrayList<AnalyzeMethod>();
				int position = argList.indexOf(METHODKEYWORD);
				while(position +1 < argList.size() && !argList.get(position+1).startsWith("-")){
					AnalyzeMethod aMethod = new AnalyzeMethod();
					aMethod.setName(argList.get(position+1));
					methodList.add(aMethod);
					position++;
				}
				aClass.setMethods(methodList);
		} else{
			System.err.println("parameter '"+ METHODKEYWORD+ "' is missing or has not enough arguments!");
			return null;
		}
			
		}
		if(argList.contains(MAINKEYWORD)){
			aClass.setHasMain(false);
		} else{
			aClass.setHasMain(true);
		}
		
		return aClass;
	}
	
	public String parseAndroidArguments(String[] args){
		List<String> argList = Arrays.asList(args);
		if(argList.contains(ANDROIDKEYWORD) && argList.indexOf(ANDROIDKEYWORD)+1 < argList.size() && !argList.get(argList.indexOf(ANDROIDKEYWORD)+1).startsWith("-")){
			return argList.get(argList.indexOf(ANDROIDKEYWORD)+1);
		}else{
			System.err.println("parameter '"+ ANDROIDKEYWORD+"' is missing or has not enough arguments!");
		}
		return "";
	}
}
