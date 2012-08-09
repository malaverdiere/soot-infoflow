package soot.jimple.infoflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import soot.jimple.infoflow.data.AnalyzeClass;
import soot.jimple.infoflow.data.AnalyzeMethod;
import soot.jimple.infoflow.util.ArgParser;

public class cmdInfoflow {

	public static void main(String[] args){
		ArgParser parser = new ArgParser();
		AnalyzeClass analyzeClass = null;
		if(args.length>0){
			if(Arrays.asList(args).contains(ArgParser.CLASSKEYWORD)){
				analyzeClass = parser.parseClassArguments(args);
			} else if (Arrays.asList(args).contains(ArgParser.ANDROIDKEYWORD)){
				//TODO: to be added by bachelor thesis
			}else{
				//just use normal args and provide default testclass
				analyzeClass = new AnalyzeClass();
				analyzeClass.setNameWithPath("Test");
				AnalyzeMethod aMethod = new AnalyzeMethod();
				aMethod.setName("main");
				List<AnalyzeMethod> methodList = new ArrayList<AnalyzeMethod>();
				methodList.add(aMethod);
				analyzeClass.setHasMain(true);
				analyzeClass.setMethods(methodList);
			
		}
		if(analyzeClass.getNameWithPath() == null || analyzeClass.getMethods().size() == 0){
			System.err.println("Parsen der Argumente war nicht erfolgreich!");
			return;
		}
		IInfoflow infoflow = new Infoflow();
		List<AnalyzeClass> classList = new ArrayList<AnalyzeClass>();
		classList.add(analyzeClass);
		infoflow.computeInfoflow("", classList,null, null);
		infoflow = null;
		}
	}
	
}
