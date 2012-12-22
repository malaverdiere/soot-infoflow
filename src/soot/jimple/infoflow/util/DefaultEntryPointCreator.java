package soot.jimple.infoflow.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soot.IntType;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JNopStmt;

public class DefaultEntryPointCreator extends BaseEntryPointCreator implements IEntryPointCreator{

	/**
	 * Soot requires a main method, so we create a dummy method which calls all entry functions.
	 * 
	 * @param classMap
	 *            the methods to call (signature as String)
	 * @param createdClass
	 *            the class which contains the methods
	 * @return list of entryPoints
	 */
	public SootMethod createDummyMain(Map<String, List<String>> classMap) {

		
		
		// create new class:
 		JimpleBody body = Jimple.v().newBody();
 		SootMethod mainMethod = createEmptyMainMethod(body);
		
 		LocalGenerator generator = new LocalGenerator(body);
		HashMap<String, Local> localVarsForClasses = new HashMap<String, Local>();
		
		// create constructors:
		for(String className : classMap.keySet()){
			SootClass createdClass = Scene.v().getSootClass(className);
			if (isConstructorGenerationPossible(createdClass)) {
				Local localVal = generateClassConstructor(createdClass, body);
				localVarsForClasses.put(className, localVal);
			}else{
				System.out.println("Alarm!! "+ createdClass);
			}
		}
		
		// add entrypoint calls
		int conditionCounter = 0;
		JNopStmt startStmt = new JNopStmt();
		JNopStmt endStmt = new JNopStmt();
		Value intCounter = generator.generateLocal(IntType.v());
		body.getUnits().add(startStmt);
		for(Entry<String, List<String>> entry : classMap.entrySet()){
			SootClass currentClass = Scene.v().getSootClass(entry.getKey());
			Local classLocal = localVarsForClasses.get(entry.getKey());
			for(SootMethod currentMethod : currentClass.getMethods()){
				if(entry.getValue().contains(currentMethod.toString())){
					JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
					conditionCounter++;
					JNopStmt thenStmt = new JNopStmt();
					JIfStmt ifStmt = new JIfStmt(cond, thenStmt);
					body.getUnits().add(ifStmt);
					JNopStmt elseStmt = new JNopStmt();
					JGotoStmt elseGoto = new JGotoStmt(elseStmt);
					body.getUnits().add(elseGoto);
					
					body.getUnits().add(thenStmt);
					buildMethodCall(currentMethod, body, classLocal, generator);
					
					body.getUnits().add(new JGotoStmt(endStmt));
					body.getUnits().add(elseStmt);
				}
			}
			
		}
		JGotoStmt gotoStmt = new JGotoStmt(endStmt);
		body.getUnits().add(gotoStmt);
		
		body.getUnits().add(endStmt);
		JGotoStmt gotoStart = new JGotoStmt(startStmt);
		body.getUnits().add(gotoStart);
		
		return mainMethod;
	}

	
//	 else {
//			// backup: simplified form:
//			
//			Local tempLocal = generator.generateLocal(RefType.v(classMap.getKey())); //or: createdClass
//			
//			System.out.println("Warning - old code executed:");
//			NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(classMap.getKey()));
//			AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, newExpr);
//			SpecialInvokeExpr sinvokeExpr = Jimple.v().newSpecialInvokeExpr(tempLocal, Scene.v().makeMethodRef(createdClass, "<init>", new ArrayList<Type>(), VoidType.v(), false));
//			body.getUnits().add(assignStmt);
//			body.getUnits().add(Jimple.v().newInvokeStmt(sinvokeExpr));
//
//			generateClassConstructor(Scene.v().getSootClass(classMap.getKey()), body,classMap.getValue());
//			
//			// TODO: also call constructor of call params:
//			for (String method : classMap.getValue()) {
//				SootMethod sMethod = Scene.v().getMethod(method);
//				entryPoints.add(sMethod);
//
//			}
//
//		}

}
