package soot.jimple.infoflow.entryPointCreators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.SootMethodAndClass;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JNopStmt;

/**
 * based on: http://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
 * and http://developer.android.com/reference/android/app/Service.html
 * and http://developer.android.com/reference/android/content/BroadcastReceiver.html#ReceiverLifecycle
 * and http://developer.android.com/reference/android/content/BroadcastReceiver.html
 * 
 * @author Christian, Steven Arzt
 * 
 */
public class AndroidEntryPointCreator extends BaseEntryPointCreator implements IEntryPointCreator{
	private JimpleBody body;
	private LocalGenerator generator;
	private int conditionCounter;
	private Value intCounter;
	
	private List<String> androidClasses;
	private List<String> callbackFunctions;
	
	/**
	 * Creates a new instance of the {@link AndroidEntryPointCreator} class.
	 */
	public AndroidEntryPointCreator() {
		this.androidClasses = new ArrayList<String>();
		this.callbackFunctions = new ArrayList<String>();
	}
	
	/**
	 * Creates a new instance of the {@link AndroidEntryPointCreator} class
	 * and registers a list of classes to be automatically scanned for Android
	 * lifecycle methods
	 * @param androidClasses The list of classes to be automatically scanned for
	 * Android lifecycle methods
	 */
	public AndroidEntryPointCreator(List<String> androidClasses) {
		this.androidClasses = androidClasses;
		this.callbackFunctions = new ArrayList<String>();
	}
	
	/**
	 * Sets the list of callback functions to be integrated into the Android
	 * lifecycle
	 * @param callbackFunctions The list of callback functions to be integrated
	 * into the Android lifecycle
	 */
	public void setCallbackFunctions(List<String> callbackFunctions) {
		this.callbackFunctions = callbackFunctions;
	}
	
	/**
	 * Creates a new dummy main method based only on the Android classes and
	 * the automatic detection of the Android lifecycle methods
	 * @return The generated dummy main method
	 */
	public SootMethod createDummyMain() {
		return createDummyMain(new ArrayList<String>());
	}

	/**
	 *  Soot requires a main method, so we create a dummy method which calls all entry functions. 
	 *  Android's components are detected and treated according to their lifecycles. This
	 *  method automatically resolves the classes containing the given methods.
	 *  
	 * @param methods The list of methods to be called inside the generated dummy main method.
	 * @return the dummyMethod which was created
	 */
	@Override
	public SootMethod createDummyMainInternal(List<String> methods){
		Map<String, List<String>> classMap =
				SootMethodRepresentationParser.v().parseClassNames(methods, false);
		for (String androidClass : this.androidClasses)
			if (!classMap.containsKey(androidClass))
				classMap.put(androidClass, new ArrayList<String>());
		
		// create new class:
 		body = Jimple.v().newBody();
 		
 		SootMethod mainMethod = createEmptyMainMethod(body);
		
		generator = new LocalGenerator(body);
		
		// add entrypoint calls
		conditionCounter = 0;
		intCounter = generator.generateLocal(IntType.v());

		JAssignStmt assignStmt = new JAssignStmt(intCounter, IntConstant.v(conditionCounter));
		body.getUnits().add(assignStmt);
		
		//prepare outer loop:
		JNopStmt outerStartStmt = new JNopStmt();
		body.getUnits().add(outerStartStmt);
		
		for(Entry<String, List<String>> entry : classMap.entrySet()){
			//no execution order given for all apps:
			JNopStmt entryExitStmt = new JNopStmt();
			JEqExpr entryCond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
			conditionCounter++;
			JIfStmt entryIfStmt = new JIfStmt(entryCond, entryExitStmt);
			body.getUnits().add(entryIfStmt);
			
			SootClass currentClass = Scene.v().forceResolve(entry.getKey(), SootClass.BODIES);
			currentClass.setApplicationClass();
			JNopStmt endClassStmt = new JNopStmt();

			boolean activity = false;
			boolean service = false;
			boolean broadcastReceiver = false;
			boolean contentProvider = false;
			boolean plain = false;

			// Check the type of this class
			List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
			for(SootClass sc : extendedClasses){
				if(sc.getName().equals(AndroidEntryPointConstants.ACTIVITYCLASS)){
					activity = true;
					break;
				}
				if(sc.getName().equals(AndroidEntryPointConstants.SERVICECLASS)){
					service = true;
					break;
				}
				if(sc.getName().equals(AndroidEntryPointConstants.BROADCASTRECEIVERCLASS)){
					broadcastReceiver = true;
					break;
				}
				if(sc.getName().equals(AndroidEntryPointConstants.CONTENTPROVIDERCLASS)){
					contentProvider = true;
					break;
				}
			}
			if(!activity && !service && !broadcastReceiver && !contentProvider)
				plain = true;

			// Check if one of the methods is instance. This tells us whether
			// we need to create a constructor invocation or not. Furthermore,
			// we collect references to the corresponding SootMethod objects.
			boolean instanceNeeded = activity || service || broadcastReceiver || contentProvider;
			Map<String, SootMethod> plainMethods = new HashMap<String, SootMethod>();
			if (!instanceNeeded || plain)
				for(String method : entry.getValue()){
					SootMethod sm = null;
					
					// Find the method. It may either be implemented directly in the
					// given class or it may be inherited from one of the superclasses.
					if(Scene.v().containsMethod(method))
						sm = Scene.v().getMethod(method);
					else {
						SootMethodAndClass methodAndClass = SootMethodRepresentationParser.v().parseSootMethodString(method);
						if (!Scene.v().containsClass(methodAndClass.getClassName())) {
							System.err.println("Class for entry point " + method + " not found, skipping...");
							continue;
						}
						sm = findMethod(Scene.v().getSootClass(methodAndClass.getClassName()),
								methodAndClass.getSubSignature());
						if (sm == null) {
							System.err.println("Method for entry point " + method + " not found in class, skipping...");
							continue;
						}
					}

					plainMethods.put(method, sm);
					if(!sm.isStatic())
						instanceNeeded = true;
				}
			
			// if we need to call a constructor, we insert the respective Jimple statement here
			if(instanceNeeded){
				if (isConstructorGenerationPossible(currentClass)) {
					Local localVal = generateClassConstructor(currentClass, body);
					localVarsForClasses.put(currentClass.getName(), localVal);
				}else{
					System.out.println("Constructor cannot be generated for " + currentClass.getName());
				}
			}
			Local classLocal = localVarsForClasses.get(entry.getKey());

			// Generate the lifecycles for the different kinds of Android classes
			if(activity)
				generateActivityLifecycle(entry.getValue(), currentClass, endClassStmt,
						classLocal);
			if(service)
				generateServiceLifecycle(entry.getValue(), currentClass, endClassStmt,
						classLocal);
			if(broadcastReceiver)
				generateBroadcastReceiverLifecycle(entry.getValue(), currentClass, endClassStmt,
						classLocal);
			if(contentProvider)
				generateContentProviderLifecycle(entry.getValue(), currentClass, endClassStmt,
						classLocal);
			if(plain){
				JNopStmt beforeClassStmt = new JNopStmt();
				body.getUnits().add(beforeClassStmt);
				for(SootMethod currentMethod : plainMethods.values()){
					if (!currentMethod.isStatic() && classLocal == null) {
						System.out.println("Skipping method " + currentMethod + " because we have no instance");
						continue;
					}
					
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
					
					createIfStmt(endClassStmt);
					body.getUnits().add(elseStmt);
					
					// Because we don't know the order of the custom statements,
					// we assume that you can loop arbitrarily
					createIfStmt(beforeClassStmt);
				}
			}
			body.getUnits().add(endClassStmt);
			//if-target for entryIf
			body.getUnits().add(entryExitStmt);
		}
		
		JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
		conditionCounter++;
		JIfStmt outerIfStmt = new JIfStmt(cond, outerStartStmt);
		body.getUnits().add(outerIfStmt);
		
		System.out.println("Generated main method:\n" + body);
		return mainMethod;
	}

	/**
	 * Generates the lifecycle for an Android content provider class
	 * @param entryPoints The list of methods to consider in this class
	 * @param currentClass The class for which to build the content provider
	 * lifecycle
	 * @param endClassStmt The statement to which to jump after completing
	 * the lifecycle
	 * @param classLocal The local referencing an instance of the current class
	 */
	private void generateContentProviderLifecycle
			(List<String> entryPoints,
			SootClass currentClass,
			JNopStmt endClassStmt,
			Local classLocal) {
		// As we don't know the order in which the different Android lifecycles
		// run, we allow for each single one of them to be skipped
		createIfStmt(endClassStmt);

		Stmt onCreateStmt = searchAndBuildMethod(AndroidEntryPointConstants.CONTENTPROVIDER_ONCREATE, currentClass, entryPoints, classLocal);
		//TODO: which methods?
		// see: http://developer.android.com/reference/android/content/ContentProvider.html
		//methods
		JNopStmt startWhileStmt = new JNopStmt();
		JNopStmt endWhileStmt = new JNopStmt();
		body.getUnits().add(startWhileStmt);
		for(SootMethod currentMethod : currentClass.getMethods()){
			if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getContentproviderLifecycleMethods().contains(currentMethod.getSubSignature())){
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
		addCallbackMethods(currentClass, classLocal);
		body.getUnits().add(endWhileStmt);
		createIfStmt(startWhileStmt);
		createIfStmt(onCreateStmt);
	}

	/**
	 * Generates the lifecycle for an Android broadcast receiver class
	 * @param entryPoints The list of methods to consider in this class
	 * @param currentClass The class for which to build the broadcast receiver
	 * lifecycle
	 * @param endClassStmt The statement to which to jump after completing
	 * the lifecycle
	 * @param classLocal The local referencing an instance of the current class
	 */
	private void generateBroadcastReceiverLifecycle
			(List<String> entryPoints,
			SootClass currentClass,
			JNopStmt endClassStmt,
			Local classLocal) {
		// As we don't know the order in which the different Android lifecycles
		// run, we allow for each single one of them to be skipped
		createIfStmt(endClassStmt);

		Stmt onReceiveStmt = searchAndBuildMethod(AndroidEntryPointConstants.BROADCAST_ONRECEIVE, currentClass, entryPoints, classLocal);
		//methods
		JNopStmt startWhileStmt = new JNopStmt();
		JNopStmt endWhileStmt = new JNopStmt();
		body.getUnits().add(startWhileStmt);
		for(SootMethod currentMethod : currentClass.getMethods()){
			if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getBroadcastLifecycleMethods().contains(currentMethod.getSubSignature())){
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
		addCallbackMethods(currentClass, classLocal);
		body.getUnits().add(endWhileStmt);
		createIfStmt(startWhileStmt);
		createIfStmt(onReceiveStmt);
	}

	/**
	 * Generates the lifecycle for an Android service class
	 * @param entryPoints The list of methods to consider in this class
	 * @param currentClass The class for which to build the service lifecycle
	 * @param endClassStmt The statement to which to jump after completing
	 * the lifecycle
	 * @param classLocal The local referencing an instance of the current class
	 */
	private void generateServiceLifecycle
			(List<String> entryPoints,
			SootClass currentClass,
			JNopStmt endClassStmt,
			Local classLocal) {
		// As we don't know the order in which the different Android lifecycles
		// run, we allow for each single one of them to be skipped
		createIfStmt(endClassStmt);

		// 1. onCreate:
		Stmt onCreateStmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONCREATE, currentClass, entryPoints, classLocal);
		
		//service has two different lifecycles:
		//lifecycle1:
		//2. onStart:
		searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONSTART1, currentClass, entryPoints, classLocal);
		searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONSTART2, currentClass, entryPoints, classLocal);
		//methods: 
		//all other entryPoints of this class:
		JNopStmt startWhileStmt = new JNopStmt();
		JNopStmt endWhileStmt = new JNopStmt();
		body.getUnits().add(startWhileStmt);
		for(SootMethod currentMethod : currentClass.getMethods()){
			if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getServiceLifecycleMethods().contains(currentMethod.getSubSignature())){
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
		addCallbackMethods(currentClass, classLocal);
		body.getUnits().add(endWhileStmt);
		createIfStmt(startWhileStmt);
		
		//lifecycle1 end
		
		//lifecycle2 start
		//onBind:
		Stmt onBindStmt = searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONBIND, currentClass, entryPoints, classLocal);
		
		JNopStmt beforemethodsStmt = new JNopStmt();
		body.getUnits().add(beforemethodsStmt);
		//methods
		JNopStmt startWhile2Stmt = new JNopStmt();
		JNopStmt endWhile2Stmt = new JNopStmt();
		body.getUnits().add(startWhile2Stmt);
		for(SootMethod currentMethod : currentClass.getMethods()){
			if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getServiceLifecycleMethods().contains(currentMethod.getSubSignature())){
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
		addCallbackMethods(currentClass, classLocal);
		body.getUnits().add(endWhile2Stmt);
		createIfStmt(startWhile2Stmt);
		
		//onRebind:
		searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONREBIND, currentClass, entryPoints, classLocal);
		createIfStmt(beforemethodsStmt);
		//onUnbind:
		searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONUNBIND, currentClass, entryPoints, classLocal);
		createIfStmt(onBindStmt);
		//lifecycle2 end
		
		//onDestroy:
		searchAndBuildMethod(AndroidEntryPointConstants.SERVICE_ONDESTROY, currentClass, entryPoints, classLocal);
		
		//either begin or end or next class:
		createIfStmt(onCreateStmt);
		createIfStmt(endClassStmt);
	}

	/**
	 * Generates the lifecycle for an Android activity
	 * @param entryPoints The list of methods to consider in this class
	 * @param currentClass The class for which to build the activity lifecycle
	 * @param endClassStmt The statement to which to jump after completing
	 * the lifecycle
	 * @param classLocal The local referencing an instance of the current class
	 */
	private void generateActivityLifecycle
			(List<String> entryPoints,
			SootClass currentClass,
			JNopStmt endClassStmt,
			Local classLocal) {
		// As we don't know the order in which the different Android lifecycles
		// run, we allow for each single one of them to be skipped
		createIfStmt(endClassStmt);
		
		// 1. onCreate:
		Stmt onCreateStmt = searchAndBuildMethod
				(AndroidEntryPointConstants.ACTIVITY_ONCREATE, currentClass, entryPoints, classLocal);
		//2. onStart:
		Stmt onStartStmt = searchAndBuildMethod
				(AndroidEntryPointConstants.ACTIVITY_ONSTART, currentClass, entryPoints, classLocal);
		searchAndBuildMethod
				(AndroidEntryPointConstants.ACTIVITY_ONRESTOREINSTANCESTATE, currentClass, entryPoints, classLocal);
		searchAndBuildMethod
				(AndroidEntryPointConstants.ACTIVITY_ONPOSTCREATE, currentClass, entryPoints, classLocal);
		//3. onResume:
		Stmt onResumeStmt = searchAndBuildMethod
				(AndroidEntryPointConstants.ACTIVITY_ONRESUME, currentClass, entryPoints, classLocal);
		searchAndBuildMethod
				(AndroidEntryPointConstants.ACTIVITY_ONPOSTRESUME, currentClass, entryPoints, classLocal);
		
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
		addCallbackMethods(currentClass, classLocal);
		
		body.getUnits().add(endWhileStmt);
		createIfStmt(startWhileStmt);
		
		//4. onPause:
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONPAUSE, currentClass, entryPoints, classLocal);
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONCREATEDESCRIPTION, currentClass, entryPoints, classLocal);
		searchAndBuildMethod(AndroidEntryPointConstants.ACTIVITY_ONSAVEINSTANCESTATE, currentClass, entryPoints, classLocal);
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

		createIfStmt(endClassStmt);
	}
	
	/**
	 * Generateds invocation statements for all callback methods which need to
	 * be invoked during the given class' run cycle.
	 * @param currentClass The class for which we currently build the lifecycle
	 * @param parentClassLocal The local containing a reference to the class
	 * for which we are currently building the lifecycle.
	 */
	private void addCallbackMethods(SootClass currentClass,
			Local parentClassLocal) {
		// Get all classes in which callback methods are declared
		Map<SootClass, Set<SootMethod>> callbackClasses = new HashMap<SootClass, Set<SootMethod>>();
		for (String methodSig : this.callbackFunctions) {
			SootMethodAndClass methodAndClass = SootMethodRepresentationParser.v().parseSootMethodString(methodSig);
			SootClass theClass = Scene.v().getSootClass(methodAndClass.getClassName());
			SootMethod theMethod = findMethod(theClass, methodAndClass.getSubSignature());
			if (theMethod == null) {
				System.err.println("Could not find callback method " + methodAndClass.getSignature());
				continue;
			}
			
			if (callbackClasses.containsKey(theClass))
				callbackClasses.get(theClass).add(theMethod);
			else {
				Set<SootMethod> methods = new HashSet<SootMethod>();
				methods.add(theMethod);
				callbackClasses.put(theClass, methods);				
			}
		}

		for (SootClass callbackClass : callbackClasses.keySet()) {
			Local classLocal;
			if (callbackClass.getName().equals(currentClass.getName()))
				classLocal = parentClassLocal;
			else {
				// Create a new instance of this class
				// if we need to call a constructor, we insert the respective Jimple statement here
				if (!isConstructorGenerationPossible(callbackClass)) {
					System.out.println("Constructor cannot be generated for callback class "
							+ callbackClass.getName());
					continue;
				}
				classLocal = generateClassConstructor(callbackClass, body);
			}
			
			// Build the calls to all callback methods in this class
			for (SootMethod callbackMethod : callbackClasses.get(callbackClass)) {
				JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
				conditionCounter++;
				JNopStmt thenStmt = new JNopStmt();
				JIfStmt ifStmt = new JIfStmt(cond, thenStmt);
				body.getUnits().add(ifStmt);
				JNopStmt elseStmt = new JNopStmt();
				JGotoStmt elseGoto = new JGotoStmt(elseStmt);
				body.getUnits().add(elseGoto);
				
				body.getUnits().add(thenStmt);
				buildMethodCall(callbackMethod, body, classLocal, generator);
	
				body.getUnits().add(elseStmt);
			}
		}
	}

	private Stmt searchAndBuildMethod(String subsignature, SootClass currentClass, List<String> entryPoints, Local classLocal){
		SootMethod method = findMethod(currentClass, subsignature);
		if (method == null) {
			System.err.println("Could not find Android entry point method: " + subsignature);
			return null;
		}
		entryPoints.remove(method.getSignature());

		assert method.isStatic() || classLocal != null : "Class local was null for non-static method "
				+ method.getSignature();
		
		//write Method
		return buildMethodCall(method, body, classLocal, generator);
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
	
	/**
	 * Finds a method with the given signature in the given class or one of its
	 * super classes
	 * @param currentClass The current class in which to start the search
	 * @param subsignature The subsignature of the method to find
	 * @return The method with the given signature if it has been found,
	 * otherwise null
	 */
	protected SootMethod findMethod(SootClass currentClass, String subsignature){
		if(currentClass.declaresMethod(subsignature)){
			return currentClass.getMethod(subsignature);
		}
		if(currentClass.hasSuperclass()){
			return findMethod(currentClass.getSuperclass(), subsignature);
		}
		return null;
	}

}
