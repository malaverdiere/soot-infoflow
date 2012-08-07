package soot.jimple.infoflow.util;

import java.util.Arrays;
import java.util.List;

public class ArgParser {
	public static String CLASSKEYWORD = "-class";
	public static String METHODKEYWORD = "-methods";
	public static String ANDROIDKEYWORD = "-android";
	public static String MAINKEYWORD = "-nomain";
	
	public ClassAndMethods parseClassArguments(String[] args){
		ClassAndMethods cam = new ClassAndMethods();
		List<String> argList = Arrays.asList(args);
		if(argList.contains(CLASSKEYWORD) && argList.indexOf(CLASSKEYWORD)+1 < argList.size() && !argList.get(argList.indexOf(CLASSKEYWORD)+1).startsWith("-")){
			cam.setClassName(argList.get(argList.indexOf(CLASSKEYWORD)+1));
			if(argList.contains(METHODKEYWORD)){ 
				int position = argList.indexOf(METHODKEYWORD);
				while(position +1 < argList.size() && !argList.get(position+1).startsWith("-")){
					cam.addMethodName(argList.get(position+1));
					position++;
				}
		} else{
			System.err.println("parameter '"+ METHODKEYWORD+ "' is missing or has not enough arguments!");
		}
			
		}
		if(argList.contains(MAINKEYWORD)){
			cam.setNomain(true);
		}
		
		return cam;
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
