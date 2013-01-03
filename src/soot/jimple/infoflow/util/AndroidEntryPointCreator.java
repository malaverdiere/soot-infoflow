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
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JNopStmt;

/**
 * based on: http://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
 * @author Christian
 *
 */
public class AndroidEntryPointCreator extends BaseEntryPointCreator implements IEntryPointCreator{
	private JimpleBody body;
	private LocalGenerator generator;
	private int conditionCounter;
	private Value intCounter;
	
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
 		body = Jimple.v().newBody();
 		
 		SootMethod mainMethod = createEmptyMainMethod(body);
		
		generator = new LocalGenerator(body);
		HashMap<String, Local> localVarsForClasses = new HashMap<String, Local>();
		
		// create constructors:
		for(String className : classMap.keySet()){
			SootClass createdClass = Scene.v().getSootClass(className);
			if (isConstructorGenerationPossible(createdClass)) {
				Local localVal = generateClassConstructor(createdClass, body);
				localVarsForClasses.put(className, localVal);
			}else{
				System.out.println("Constructor cannot be generated for "+ createdClass);
			}
		}
		
		// add entrypoint calls
		conditionCounter = 0;
		intCounter = generator.generateLocal(IntType.v());
		for(Entry<String, List<String>> entry : classMap.entrySet()){
			SootClass currentClass = Scene.v().getSootClass(entry.getKey());
			Local classLocal = localVarsForClasses.get(entry.getKey());
			JNopStmt endClassStmt = new JNopStmt();
			//if currentClass extends Activity use activity model
			boolean activity = false;
			Scene.v().getSootClass("android.app.Activity");
			List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
			for(SootClass sc :extendedClasses){
				if(sc.getName().equals("android.app.Activity")){
					activity = true;
				}
			}
			//############################ ACTIVITY ####################################################
			if(activity){
				//analyse entryPoints:
				List<String> entryPoints = entry.getValue();
				
				// 1. onCreate:
				JNopStmt onCreateStmt = searchAndBuildMethod("<"+ currentClass.toString() +": void onCreate(android.os.Bundle)>", currentClass, entryPoints, classLocal);
				//2. onStart:
				JNopStmt onStartStmt = searchAndBuildMethod("<"+ currentClass.toString() +": void onStart()>", currentClass, entryPoints, classLocal);
				//3. onResume:
				JNopStmt onResumeStmt = searchAndBuildMethod("<"+ currentClass.toString() +": void onResume()>", currentClass, entryPoints, classLocal);
				
				JNopStmt runningStart = new JNopStmt();
				JGotoStmt runningGoto = new JGotoStmt(runningStart);
				body.getUnits().add(runningGoto);
				JNopStmt runningEnd = new JNopStmt();
				body.getUnits().add(runningEnd);
				
				//4. onPause:
				searchAndBuildMethod("<"+ currentClass.toString() +": void onPause()>", currentClass, entryPoints, classLocal);
				//goTo Stop, Resume or Create:
				JNopStmt pauseToStopStmt = new JNopStmt();
				createIfStmt(pauseToStopStmt);
				createIfStmt(onResumeStmt);
				createIfStmt(onCreateStmt);
			
				body.getUnits().add(pauseToStopStmt);
				//5. onStop:
				searchAndBuildMethod("<"+ currentClass.toString() +": void onStop()>", currentClass, entryPoints, classLocal);
				//goTo onDestroy, onRestart or onCreate:
				conditionCounter++;
				JNopStmt stopToDestroyStmt = new JNopStmt();
				JNopStmt stopToRestartStmt = new JNopStmt();
				createIfStmt(stopToDestroyStmt);
				createIfStmt(stopToRestartStmt);
				createIfStmt(onCreateStmt);
				
				//6. onRestart:
				body.getUnits().add(stopToRestartStmt);
				searchAndBuildMethod("<"+ currentClass.toString() +": void onRestart()>", currentClass, entryPoints, classLocal);
				JGotoStmt startGoto = new JGotoStmt(onStartStmt);
				body.getUnits().add(startGoto);
				
				//7. onDestroy
				body.getUnits().add(stopToDestroyStmt);
				searchAndBuildMethod("<"+ currentClass.toString() +": void onDestroy()>", currentClass, entryPoints, classLocal);
				JGotoStmt endGoto = new JGotoStmt(endClassStmt);
				body.getUnits().add(endGoto);
				
				
				//all other entryPoints of this class:
				body.getUnits().add(runningStart);
				JNopStmt startWhileStmt = new JNopStmt();
				JNopStmt endWhileStmt = new JNopStmt();
				body.getUnits().add(startWhileStmt);
				
				for(SootMethod currentMethod : currentClass.getMethods()){
					if(entryPoints.contains(currentMethod.toString())){
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

						body.getUnits().add(new JGotoStmt(endWhileStmt));
						body.getUnits().add(elseStmt);
					}
				}
				JGotoStmt gotoStmt = new JGotoStmt(endWhileStmt);
				body.getUnits().add(gotoStmt);
				
				body.getUnits().add(endWhileStmt);
				JGotoStmt gotoStart = new JGotoStmt(startWhileStmt);
				body.getUnits().add(gotoStart);
				
				
				JGotoStmt lifecycleGoto = new JGotoStmt(runningEnd);
				body.getUnits().add(lifecycleGoto);
				
			}
			
			
			body.getUnits().add(endClassStmt);
		}
		
		
	
		
		return mainMethod;
	}
	
	private JNopStmt searchAndBuildMethod(String signature, SootClass currentClass, List<String> entryPoints, Local classLocal){
		SootMethod onMethod = currentClass.getMethod(signature);
		JNopStmt onMethodStmt = new JNopStmt();
		body.getUnits().add(onMethodStmt);
		if(onMethod != null){
			//write Method
			buildMethodCall(onMethod, body, classLocal, generator);
			for(int i=0; i< entryPoints.size(); i++){
				if(entryPoints.get(i).contains(signature)){
					entryPoints.remove(i);
					i--;
				}
			}
		}
		return onMethodStmt;
	}
	
	private void createIfStmt(Unit target){
		JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
		conditionCounter++;
		JIfStmt ifStmt = new JIfStmt(cond, target);
		body.getUnits().add(ifStmt);
	}
	

}