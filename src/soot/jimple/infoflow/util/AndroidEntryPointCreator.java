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
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JNopStmt;

/**
 * based on: http://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
 * and http://developer.android.com/reference/android/app/Service.html
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
			boolean service = false;
			//broadcastreceiver has only one method in lifecycle - so we use default methodcreator
			//see: http://developer.android.com/reference/android/content/BroadcastReceiver.html
			
			//Scene.v().getSootClass("android.app.Activity");
			List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
			for(SootClass sc :extendedClasses){
				if(sc.getName().equals("android.app.Activity")){
					activity = true;
				}
				if(sc.getName().equals("android.app.Service")){
					service = true;
				}
				
			}
			//TODO: service und activity gleichzeitig implementiert - geht das?
			if(activity){
				//analyse entryPoints:
				List<String> entryPoints = entry.getValue();
				
				//test:
				JAssignStmt assignStmt = new JAssignStmt(intCounter, IntConstant.v(conditionCounter));
				body.getUnits().add(assignStmt);
				
				// 1. onCreate:
				JNopStmt onCreateStmt = searchAndBuildMethod("void onCreate(android.os.Bundle)", currentClass, entryPoints, classLocal);
				//2. onStart:
				JNopStmt onStartStmt = searchAndBuildMethod("void onStart()", currentClass, entryPoints, classLocal);
				//3. onResume:
				JNopStmt onResumeStmt = searchAndBuildMethod("void onResume()", currentClass, entryPoints, classLocal);
				
				JNopStmt runningStart = new JNopStmt();
				JGotoStmt runningGoto = new JGotoStmt(runningStart);
				
				
				body.getUnits().add(runningGoto);
				JNopStmt runningEnd = new JNopStmt();
				body.getUnits().add(runningEnd);
				
				//4. onPause:
				searchAndBuildMethod("void onPause()", currentClass, entryPoints, classLocal);
				//goTo Stop, Resume or Create:
				JNopStmt pauseToStopStmt = new JNopStmt();
				createIfStmt(pauseToStopStmt);
				createIfStmt(onResumeStmt);
				createIfStmt(onCreateStmt);
				
				body.getUnits().add(pauseToStopStmt);
				//5. onStop:
				searchAndBuildMethod("void onStop()", currentClass, entryPoints, classLocal);
				//goTo onDestroy, onRestart or onCreate:
				conditionCounter++;
				JNopStmt stopToDestroyStmt = new JNopStmt();
				JNopStmt stopToRestartStmt = new JNopStmt();
				createIfStmt(stopToDestroyStmt);
				createIfStmt(stopToRestartStmt);
				createIfStmt(onCreateStmt);
				
				//6. onRestart:
				body.getUnits().add(stopToRestartStmt);
				searchAndBuildMethod("void onRestart()", currentClass, entryPoints, classLocal);
				if(onStartStmt != null){
					JGotoStmt startGoto = new JGotoStmt(onStartStmt);
					body.getUnits().add(startGoto);
				}
				
				//7. onDestroy
				body.getUnits().add(stopToDestroyStmt);
				searchAndBuildMethod("void onDestroy()", currentClass, entryPoints, classLocal);
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
				createIfStmt(startWhileStmt);
//				JGotoStmt gotoStart = new JGotoStmt(startWhileStmt);
//				body.getUnits().add(gotoStart);
				
				
				JGotoStmt lifecycleGoto = new JGotoStmt(runningEnd);
				body.getUnits().add(lifecycleGoto);
				
			}else if(service){
				//analyse entryPoints:
				List<String> entryPoints = entry.getValue();
				
				// 1. onCreate:
				JNopStmt onCreateStmt = searchAndBuildMethod("void onCreate()", currentClass, entryPoints, classLocal);
				
				//service has two different lifecycles:
				//lifecycle1:
				//2. onStart:
				JNopStmt onStartStmt = searchAndBuildMethod("void onStart(android.content.Intent, int)", currentClass, entryPoints, classLocal);
				JNopStmt onStart2Stmt = searchAndBuildMethod("int onStartCommand(android.content.Intent, int, int)", currentClass, entryPoints, classLocal);
				//methods
				//lifecycle1 end
				
				//lifecycle2 start
				
				//onBind:
				JNopStmt onBindStmt = searchAndBuildMethod("android.os.IBinder onBind(android.content.Intent)", currentClass, entryPoints, classLocal);
				//methods
				
				//onRebind:
				JNopStmt onRebindStmt = searchAndBuildMethod("void onBind(android.content.Intent)", currentClass, entryPoints, classLocal);
				
				//onUnbind:
				JNopStmt onUnbindStmt = searchAndBuildMethod("boolean onUnbind(android.content.Intent)", currentClass, entryPoints, classLocal);
				
				//lifecycle2 end
				
				//onDestroy:
				searchAndBuildMethod("void onDestroy()", currentClass, entryPoints, classLocal);
				
			}else{
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
						
						body.getUnits().add(new JGotoStmt(endClassStmt)); //TODO: correct?
						body.getUnits().add(elseStmt);
					}
				}
				
			}
			
			
			body.getUnits().add(endClassStmt);
		}
		
		
	
		
		return mainMethod;
	}
	
	private JNopStmt searchAndBuildMethod(String signature, SootClass currentClass, List<String> entryPoints, Local classLocal){
		if(currentClass.declaresMethod(signature)){
			SootMethod onMethod = currentClass.getMethod(signature);
			if(onMethod != null){
				JNopStmt onMethodStmt = new JNopStmt();
				body.getUnits().add(onMethodStmt);
				
				//write Method
				buildMethodCall(onMethod, body, classLocal, generator);
				entryPoints.remove("<"+ currentClass.toString()+ ": "+signature+">");
				return onMethodStmt;
			}
		}
		return null;
	}
	
	private void createIfStmt(Unit target){
		if(target == null){
			return;
		}
		JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
		conditionCounter++;
		JIfStmt ifStmt = new JIfStmt(cond, target);
		body.getUnits().add(ifStmt);
//		JAssignStmt assignStmt = new JAssignStmt(intCounter, IntConstant.v(conditionCounter));
//		body.getUnits().add(assignStmt);
	}
	

}