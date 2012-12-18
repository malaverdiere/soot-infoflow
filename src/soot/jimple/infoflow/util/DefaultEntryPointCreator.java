package soot.jimple.infoflow.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soot.BooleanType;
import soot.G;
import soot.IntType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.dava.internal.javaRep.DIntConstant;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JNopStmt;

public class DefaultEntryPointCreator implements IEntryPointCreator{

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
 		SootMethod mainMethod = new SootMethod("dummyMainMethod", new ArrayList<Type>(), VoidType.v());
		body.setMethod(mainMethod);
		mainMethod.setActiveBody(body);
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
			Local tempLocal = localVarsForClasses.get(entry.getKey());
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
					Local stringLocal = null;
					//currentMethod.setDeclaringClass(currentClass);
					VirtualInvokeExpr vInvokeExpr;
					if(currentMethod.getParameterCount()>0){
						List<Object> args = new LinkedList<Object>();
						for(Object ob :currentMethod.getParameterTypes()){
							if(ob instanceof Type){
								Type t = (Type)ob;
								SootClass classToType = Scene.v().getSootClass(t.toString());
								if(classToType != null){
									Value val = generateClassConstructor(classToType, body);
									if(val != null)
										args.add(val);
								}
							}
						}
						vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, currentMethod.makeRef(),args);
					}else{
						vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, currentMethod.makeRef());
					}
					 
					Stmt stmt;
					if (!(currentMethod.getReturnType() instanceof VoidType)) {
						stringLocal = generator.generateLocal(currentMethod.getReturnType());
						stmt = Jimple.v().newAssignStmt(stringLocal, vInvokeExpr);
						
					} else {
						stmt = Jimple.v().newInvokeStmt(vInvokeExpr);
					}
					body.getUnits().add(stmt);
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
		
		// create dummy class/method:
		
		
		SootClass mainClass = new SootClass("dummyMainClass");
		mainClass.setApplicationClass();
		mainClass.addMethod(mainMethod);
		Scene.v().addClass(mainClass);
		
		
		return mainMethod;
	}

	private boolean isConstructorGenerationPossible(SootClass sClass) {
		if (sClass == null) {
			return false;
		}
		if(isSimpleType(sClass.toString())){
			return true;
		}
		
		// look for constructors:
		boolean oneOk = false; // we need at least one constructor that works
		List<SootMethod> methodList = (List<SootMethod>) sClass.getMethods();
		for (SootMethod currentMethod : methodList) {
			if (!currentMethod.isPrivate() && currentMethod.isConstructor()) {
				boolean canGenerateConstructor = true;
				@SuppressWarnings("unchecked")
				List<Type> typeList = (List<Type>) currentMethod.getParameterTypes();
				for (Type type : typeList) {
					String typeName = type.toString().replaceAll("\\[\\]]", "");
					// 1. Type not available:
					if (!Scene.v().containsClass(typeName)) {
						canGenerateConstructor = false;
						break;
					} else {
						SootClass typeClass = Scene.v().getSootClass(typeName);
						// 2. Type not public:
						if (typeClass.isPrivate()) {
							canGenerateConstructor = false;
							break;
						}
						//no loops:
						if(sClass.equals(typeClass)){
							System.out.println("Found constructor-loop for class " + type);
							//canGenerateConstructor = false;
							break;
						}
						// we have to recursively check this type, too:
						if (!typeClass.isJavaLibraryClass() && !isConstructorGenerationPossible(typeClass)) { // TODO: is this okay for "primitive datatypes and others - maybe not because List<CustomType> has to be created, too?
							canGenerateConstructor = false;
							break;
						}
					}

					// -> no nicer way for this?
				}
				if (canGenerateConstructor) {
					oneOk = true;
				} else{
					
				}

			}

		}
		return oneOk;
	}

	private Local generateClassConstructor(SootClass createdClass, JimpleBody body) {
		// if sootClass is simpleClass:
		if (DefaultEntryPointCreator.isSimpleType(createdClass.toString())) {
			LocalGenerator generator = new LocalGenerator(body);
			Local varLocal =  generator.generateLocal(createdClass.getType());
			
			AssignStmt aStmt = Jimple.v().newAssignStmt(varLocal, getSimpleDefaultValue(createdClass.toString()));
			body.getUnits().add(aStmt);
			return varLocal;
		} else {

			List<SootMethod> methodList =  createdClass.getMethods();
			Local returnLocal = null;
			LocalGenerator generator = new LocalGenerator(body);
			Local tempLocal = generator.generateLocal(RefType.v(createdClass));
			
			NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(createdClass));
			AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, newExpr);
			body.getUnits().add(assignStmt);
			
			
			for (SootMethod currentMethod : methodList) {
				if (!currentMethod.isPrivate() && currentMethod.isConstructor()) {
					@SuppressWarnings("unchecked")
					List<Type> typeList = (List<Type>) currentMethod.getParameterTypes();
					List<Object> params = new LinkedList<Object>();
					for (Type type : typeList) {
						String typeName = type.toString().replaceAll("\\[\\]]", "");
						if (Scene.v().containsClass(typeName)) {
							SootClass typeClass = Scene.v().getSootClass(typeName);
							// 2. Type not public:
							if (!typeClass.isPrivate() && !typeClass.toString().equals(createdClass.toString())) { // avoid loops
								params.add(generateClassConstructor(typeClass, body));
							}else{
								params.add(NullConstant.v());
							}
						} else {
							if(typeName.equals("int")){
								Local varLocal =  generator.generateLocal(IntType.v());
								
								AssignStmt aStmt = Jimple.v().newAssignStmt(varLocal, getSimpleDefaultValue(typeName));
								body.getUnits().add(aStmt);
								params.add(varLocal);
							}else if(typeName.equals("boolean")){
								Local varLocal =  generator.generateLocal(BooleanType.v());
								
								AssignStmt aStmt = Jimple.v().newAssignStmt(varLocal, getSimpleDefaultValue(typeName));
								body.getUnits().add(aStmt);
								params.add(varLocal);
							}else{
								System.out.println("Warnung - Type not found: "+typeName);
								params.add(generateClassConstructor(Scene.v().getSootClass("java.lang.Object"), body));
							}
						}
					}
					VirtualInvokeExpr vInvokeExpr;
					
					if (params.isEmpty() || params.contains(null)) {
						vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, currentMethod.makeRef());
					} else {
						vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, currentMethod.makeRef(), params);
					}
					if (!(currentMethod.getReturnType() instanceof VoidType)) { 
						Local possibleReturn = generator.generateLocal(currentMethod.getReturnType());
						if(possibleReturn != null){ //we only need one local that is != null
							returnLocal = possibleReturn;
						}
						AssignStmt assignStmt2 = Jimple.v().newAssignStmt(returnLocal, vInvokeExpr);
						body.getUnits().add(assignStmt2);
					} else {
						body.getUnits().add(Jimple.v().newInvokeStmt(vInvokeExpr));
					}
				} 
			}
			return tempLocal;
		}
	}

	private static boolean isSimpleType(String t) {
		if (t.equals("java.lang.String") || t.equals("void") || t.equals("char") || t.equals("byte") || t.equals("short") || t.equals("int") || t.equals("float") || t.equals("long") || t.equals("double") || t.equals("boolean")) {
			return true;
		} else {
			return false;
		}
	}

	private Value getSimpleDefaultValue(String t) {
		if (t.equals("boolean")) {
			return DIntConstant.v(0, BooleanType.v());
		} else if (t.equals("java.lang.String")) {
			return StringConstant.v("");
		} else if (t.equals("int")) {
			return IntConstant.v(0);
		} else if (t.equals("long")){
			return LongConstant.v(0);
		} else if (t.equals("double")){
			return DoubleConstant.v(0);
		} else if (t.equals("float")){
			return FloatConstant.v(0);
		}

		//also for arrays etc.
		return G.v().soot_jimple_NullConstant();

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
