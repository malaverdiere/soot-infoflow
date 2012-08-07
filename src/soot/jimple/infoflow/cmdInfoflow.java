package soot.jimple.infoflow;

import java.util.Arrays;

import soot.jimple.infoflow.util.ArgParser;
import soot.jimple.infoflow.util.ClassAndMethods;

public class cmdInfoflow {

	public static void main(String[] args){
		ArgParser parser = new ArgParser();
		ClassAndMethods classmethods = null;
		if(args.length>0){
			if(Arrays.asList(args).contains(ArgParser.CLASSKEYWORD)){
				classmethods = parser.parseClassArguments(args);
			} else if (Arrays.asList(args).contains(ArgParser.ANDROIDKEYWORD)){
				//TODO: to be added by bachelor thesis
			}else{
				//just use normal args and provide default testclass
				classmethods = new ClassAndMethods();
				classmethods.setClassName("Test");
				classmethods.addMethodName("main");
				classmethods.setNomain(false);
			}
		}
		IInfoflow infoflow = new Infoflow();
		infoflow.computeInfoflow(classmethods.getClassName(), !classmethods.isNomain(), classmethods.getMethodNames());
	}
	
}
