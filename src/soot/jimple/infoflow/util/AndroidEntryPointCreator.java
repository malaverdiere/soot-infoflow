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
 * 
 * broadcastreceiver has only one method in lifecycle - so we use default methodcreator
 *	see: http://developer.android.com/reference/android/content/BroadcastReceiver.html
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

		JAssignStmt assignStmt = new JAssignStmt(intCounter, IntConstant.v(conditionCounter));
		body.getUnits().add(assignStmt);
		
		
		for(Entry<String, List<String>> entry : classMap.entrySet()){
			SootClass currentClass = Scene.v().getSootClass(entry.getKey());
			Local classLocal = localVarsForClasses.get(entry.getKey());
			JNopStmt endClassStmt = new JNopStmt();
			//if currentClass extends Activity use activity model
			boolean activity = false;
			boolean service = false;
			boolean broadcastReceiver = false;
			boolean contentProvider = false;
			boolean plain = false;
			
			//Scene.v().getSootClass("android.app.Activity");
			List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
			for(SootClass sc :extendedClasses){
				if(sc.getName().equals(AndroidEntryPointConstants.ACTIVITYCLASS)){
					activity = true;
				}
				if(sc.getName().equals(AndroidEntryPointConstants.SERVICECLASS)){
					service = true;
				}
				
			}
			if(!activity && !service && !broadcastReceiver && !contentProvider){
				plain = true;
			}
			
			if(activity){
				//analyse entryPoints:
				List<String> entryPoints = entry.getValue();
				
				// 1. onCreate:
				JNopStmt onCreateStmt = searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONCREATE, currentClass, entryPoints, classLocal);
				//2. onStart:
				JNopStmt onStartStmt = searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONSTART, currentClass, entryPoints, classLocal);
				//3. onResume:
				JNopStmt onResumeStmt = searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONRESUME, currentClass, entryPoints, classLocal);
	
				
				//all other entryPoints of this class:
				JNopStmt startWhileStmt = new JNopStmt();
				JNopStmt endWhileStmt = new JNopStmt();
				body.getUnits().add(startWhileStmt);
				
				for(SootMethod currentMethod : currentClass.getMethods()){
					if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getActivityLifecycleMethods().contains(currentMethod.getSubSignature())){
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

						body.getUnits().add(elseStmt);
					}
				}

				
				body.getUnits().add(endWhileStmt);
				createIfStmt(startWhileStmt);
				
				//4. onPause:
				searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONPAUSE, currentClass, entryPoints, classLocal);
				//goTo Stop, Resume or Create:
				JNopStmt pauseToStopStmt = new JNopStmt();
				createIfStmt(pauseToStopStmt);
				createIfStmt(onResumeStmt);
				createIfStmt(onCreateStmt);
				
				body.getUnits().add(pauseToStopStmt);
				//5. onStop:
				searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONSTOP, currentClass, entryPoints, classLocal);
				//goTo onDestroy, onRestart or onCreate:
				conditionCounter++;
				JNopStmt stopToDestroyStmt = new JNopStmt();
				JNopStmt stopToRestartStmt = new JNopStmt();
				createIfStmt(stopToDestroyStmt);
				createIfStmt(stopToRestartStmt);
				createIfStmt(onCreateStmt);
				
				//6. onRestart:
				body.getUnits().add(stopToRestartStmt);
				searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONRESTART, currentClass, entryPoints, classLocal);
				if(onStartStmt != null){
					JGotoStmt startGoto = new JGotoStmt(onStartStmt);
					body.getUnits().add(startGoto);
				}
				
				//7. onDestroy
				body.getUnits().add(stopToDestroyStmt);
				searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONDESTROY, currentClass, entryPoints, classLocal);
				JGotoStmt endGoto = new JGotoStmt(endClassStmt);
				body.getUnits().add(endGoto);
				
				
			}
			if(service){
				//analyse entryPoints:
				List<String> entryPoints = entry.getValue();
				
				// 1. onCreate:
				JNopStmt onCreateStmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONCREATE, currentClass, entryPoints, classLocal);
				
				//service has two different lifecycles:
				//lifecycle1:
				//2. onStart:
				JNopStmt onStartStmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONSTART1, currentClass, entryPoints, classLocal);
				JNopStmt onStart2Stmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONSTART2, currentClass, entryPoints, classLocal);
				//methods
				//lifecycle1 end
				
				//lifecycle2 start
				
				//onBind:
				JNopStmt onBindStmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONBIND, currentClass, entryPoints, classLocal);
				//methods
				
				//onRebind:
				JNopStmt onRebindStmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONREBIND, currentClass, entryPoints, classLocal);
				
				//onUnbind:
				JNopStmt onUnbindStmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONUNBIND, currentClass, entryPoints, classLocal);
				
				//lifecycle2 end
				
				//onDestroy:
				searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONDESTROY, currentClass, entryPoints, classLocal);
				
			}
			if(plain){
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
	
	private JNopStmt searchAndBuildMethod(String subsignature, SootClass currentClass, List<String> entryPoints, Local classLocal){
		if(currentClass.declaresMethod(subsignature)){
			return getMethodStmt(subsignature, currentClass, entryPoints, classLocal);
		}else{
			//look in history (at least the framework method itself will implement class:
			List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
			for(SootClass sclass : extendedClasses){
				if(sclass.declaresMethod(subsignature)){
					return getMethodStmt(subsignature, sclass, entryPoints, classLocal);
				}
			}		
		}
		//should not happen because customActivities extends the framework Activity
		return null;
	}
	
	private JNopStmt getMethodStmt(String signature, SootClass sclass, List<String> entryPoints, Local classLocal){
		SootMethod onMethod = sclass.getMethod(signature);
		
		JNopStmt onMethodStmt = new JNopStmt();
		body.getUnits().add(onMethodStmt);
			
		//write Method
		buildMethodCall(onMethod, body, classLocal, generator);
		entryPoints.remove("<"+ sclass.toString()+ ": "+signature+">");
		return onMethodStmt;
	}
	
	private void createIfStmt(Unit target){
		if(target == null){
			return;
		}
		JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
		conditionCounter++;
		JIfStmt ifStmt = new JIfStmt(cond, target);
		body.getUnits().add(ifStmt);
	}
	

}