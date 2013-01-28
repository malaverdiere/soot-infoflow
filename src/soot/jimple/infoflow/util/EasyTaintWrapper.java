package soot.jimple.infoflow.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
/**
 * A list of methods is passed which contains signatures of methods which taint their base objects if they are called with a tainted parameter
 * When a base object is tainted, all return values are tainted, too.
 * 
 * @author Christian
 *
 */
public class EasyTaintWrapper implements ITaintPropagationWrapper {
	private HashMap<String, List<String>> classList;

	public EasyTaintWrapper(HashMap<String, List<String>> l){
		classList = l;
	}
	
	public EasyTaintWrapper(File f){
		if(f.exists()){
			try{
				FileReader freader = new FileReader(f);
				BufferedReader reader = new BufferedReader(freader);
				String line = reader.readLine();
				SootMethodRepresentationParser parser = new SootMethodRepresentationParser();
				List<String> methodList = new LinkedList<String>();
				while(line != null){
					methodList.add(line);
					line = reader.readLine();
				}
				classList = parser.parseClassNames(methodList, true);
			}catch(IOException e){
				System.err.println("Could not read file " + f+ "! "+e);
			}
			
		}else{
			System.err.println("File "+ f + " does not exist!");
		}
	}
	
	//TODO: only classes from jdk etc
	@Override
	public boolean supportsTaintWrappingForClass(SootClass c) {
		if(classList.containsKey(c.getName())){
			return true;
		}
		if(!c.isInterface()){
			List<SootClass> superclasses = Scene.v().getActiveHierarchy().getSuperclassesOf(c);
			for(SootClass sclass : superclasses){
				if(classList.containsKey(sclass.getName()))
					return true;
			}
		}
		for(String interfaceString : classList.keySet()){
			if(c.implementsInterface(interfaceString))
				return true;
		}
		
		return false;
	}

	@Override
	public Value getTaintForMethod(Stmt stmt, int taintedparam, Value taintedBase) {
		//if param is tainted && classList contains classname && if list. contains signature of method -> add propagation
		if(taintedparam >= 0){
			SootMethod method = stmt.getInvokeExpr().getMethod();
			List<String> methodList = getMethodsForClass(method.getDeclaringClass());
		
			if(methodList.contains(method.getSubSignature())){
				if(stmt.getInvokeExprBox().getValue() instanceof InstanceInvokeExpr){
					return ((InstanceInvokeExpr) stmt.getInvokeExprBox().getValue()).getBase();
				}else if(stmt.getInvokeExprBox().getValue() instanceof StaticInvokeExpr){ //TODO: static
					if(stmt instanceof JAssignStmt){
						return ((JAssignStmt)stmt).getLeftOp();
					}
				}
			}
		}
		if(taintedBase != null){
			if(stmt instanceof JAssignStmt){
				return ((JAssignStmt)stmt).getLeftOp();
			}
		}
		return null;
	}
	
	public List<String> getMethodsForClass(SootClass c){
		List<String> methodList = new LinkedList<String>();
		if(classList.containsKey(c.getName())){
			methodList.addAll(classList.get(c.getName()));
		}
		if(!c.isInterface()){
			List<SootClass> superclasses = Scene.v().getActiveHierarchy().getSuperclassesOf(c);
			for(SootClass sclass : superclasses){
				if(classList.containsKey(sclass.getName()))
					methodList.addAll(classList.get(sclass.getName()));
			}
		}

		for(String interfaceString : classList.keySet()){
			if(c.implementsInterface(interfaceString))
				methodList.addAll(classList.get(interfaceString));
		}
		return methodList;
	}

}