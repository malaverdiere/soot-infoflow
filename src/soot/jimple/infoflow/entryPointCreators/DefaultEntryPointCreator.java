package soot.jimple.infoflow.entryPointCreators;

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
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JNopStmt;

public class DefaultEntryPointCreator extends BaseEntryPointCreator {

	/**
	 * Soot requires a main method, so we create a dummy method which calls all entry functions.
	 * 
	 * @param classMap
	 *            the methods to call (signature as String)
	 * @param createdClass
	 *            the class which contains the methods
	 * @return list of entryPoints
	 */
	@Override
	public SootMethod createDummyMainInternal(List<String> methods) {
		Map<String, List<String>> classMap =
				SootMethodRepresentationParser.v().parseClassNames(methods, false);
		
		// create new class:
 		JimpleBody body = Jimple.v().newBody();
 		SootMethod mainMethod = createEmptyMainMethod(body);
		
 		LocalGenerator generator = new LocalGenerator(body);
		HashMap<String, Local> localVarsForClasses = new HashMap<String, Local>();
		
		// create constructors:
		for(String className : classMap.keySet()){
			SootClass createdClass = Scene.v().forceResolve(className, SootClass.BODIES);
			createdClass.setApplicationClass();
			
			Local localVal = generateClassConstructor(createdClass, body);
			if (localVal == null) {
				System.out.println("Cannot generate constructor for class: "+ createdClass);
				continue;
			}
			localVarsForClasses.put(className, localVal);
		}
		
		// add entrypoint calls
		int conditionCounter = 0;
		JNopStmt startStmt = new JNopStmt();
		JNopStmt endStmt = new JNopStmt();
		Value intCounter = generator.generateLocal(IntType.v());
		body.getUnits().add(startStmt);
		for (Entry<String, List<String>> entry : classMap.entrySet()){
			Local classLocal = localVarsForClasses.get(entry.getKey());
			for (String method : entry.getValue()){
				if (!Scene.v().containsMethod(method)) {
					System.err.println("Entry point not found: " + method);
					continue;
				}
				SootMethod currentMethod = Scene.v().getMethod(method);
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
		JGotoStmt gotoStmt = new JGotoStmt(endStmt);
		body.getUnits().add(gotoStmt);
		
		body.getUnits().add(endStmt);
		JGotoStmt gotoStart = new JGotoStmt(startStmt);
		body.getUnits().add(gotoStart);
		
		return mainMethod;
	}

}