package soot.jimple.infoflow.entryPointCreators;

import java.util.ArrayList;
import java.util.Collections;
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
	private static final boolean DEBUG = true;
	
	private JimpleBody body;
	private LocalGenerator generator;
	private int conditionCounter;
	private Value intCounter;
	
	private List<String> androidClasses;
	private Map<String, List<String>> callbackFunctions;
	
	/**
	 * Array containing all types of components supported in Android lifecycles
	 */
	private enum ComponentType {
		Activity,
		Service,
		BroadcastReceiver,
		ContentProvider,
		Plain
	}
	
	/**
	 * Creates a new instance of the {@link AndroidEntryPointCreator} class.
	 */
	public AndroidEntryPointCreator() {
		this.androidClasses = new ArrayList<String>();
		this.callbackFunctions = new HashMap<String, List<String>>();
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
		this.callbackFunctions = new HashMap<String, List<String>>();
	}
	
	/**
	 * Sets the list of callback functions to be integrated into the Android
	 * lifecycle
	 * @param callbackFunctions The list of callback functions to be integrated
	 * into the Android lifecycle. This is a mapping from the Android element
	 * class (activity, service, etc.) to the list of callback methods for that
	 * element.
	 */
	public void setCallbackFunctions(Map<String, List<String>> callbackFunctions) {
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

		// Resolve all requested classes
		for (Entry<String, List<String>> entry : classMap.entrySet())
			Scene.v().forceResolve(entry.getKey(), SootClass.BODIES);
		
		// For some weird reason unknown to anyone except the flying spaghetti
		// monster, the onCreate() methods of content providers run even before
		// the application object's onCreate() is called.
		{
			JNopStmt beforeContentProvidersStmt = new JNopStmt();
			body.getUnits().add(beforeContentProvidersStmt);
			for(String className : classMap.keySet()) {
				SootClass currentClass = Scene.v().getSootClass(className);
				if (getComponentType(currentClass) == ComponentType.ContentProvider) {
					// Create an instance of the content provider
					Local localVal = generateClassConstructor(currentClass, body);
					if (localVal == null) {
						System.err.println("Constructor cannot be generated for " + currentClass.getName());
						continue;
					}
					localVarsForClasses.put(currentClass.getName(), localVal);
					
					// Conditionally call the onCreate method
					JNopStmt thenStmt = new JNopStmt();
					createIfStmt(thenStmt);
					buildMethodCall(findMethod(currentClass, AndroidEntryPointConstants.CONTENTPROVIDER_ONCREATE),
							body, localVal, generator);
					body.getUnits().add(thenStmt);
				}
			}
			// Jump back to the beginning of this section to overapproximate the
			// order in which the methods are called
			createIfStmt(beforeContentProvidersStmt);
		}
		
		// If we have an application, we need to start it in the very beginning
		SootClass applicationClass = null;
		Local applicationLocal = null;
		for (Entry<String, List<String>> entry : classMap.entrySet()) {
			SootClass currentClass = Scene.v().getSootClass(entry.getKey());
			List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
			for(SootClass sc : extendedClasses)
				if(sc.getName().equals(AndroidEntryPointConstants.APPLICATIONCLASS)) {
					applicationClass = currentClass;
					
					// Create the application
					applicationLocal = generateClassConstructor(applicationClass, body);
					if (applicationLocal == null) {
						System.err.println("Constructor cannot be generated for application class "
								+ applicationClass.getName());
						continue;
					}
					localVarsForClasses.put(applicationClass.getName(), applicationLocal);
					
					// Call the onCreate() method
					searchAndBuildMethod(AndroidEntryPointConstants.APPLICATION_ONCREATE,
							applicationClass, entry.getValue(), applicationLocal);					
					break;
				}
		}
		
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
			
			SootClass currentClass = Scene.v().getSootClass(entry.getKey());
			currentClass.setApplicationClass();
			JNopStmt endClassStmt = new JNopStmt();

			try {
				ComponentType componentType = getComponentType(currentClass);
		
				// Check if one of the methods is instance. This tells us whether
				// we need to create a constructor invocation or not. Furthermore,
				// we collect references to the corresponding SootMethod objects.
				boolean instanceNeeded = componentType == ComponentType.Activity
						|| componentType == ComponentType.Service
						|| componentType == ComponentType.BroadcastReceiver
						|| componentType == ComponentType.ContentProvider;
				Map<String, SootMethod> plainMethods = new HashMap<String, SootMethod>();
				if (!instanceNeeded)
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
				if (instanceNeeded && !localVarsForClasses.containsKey(currentClass.getName())){
					Local localVal = generateClassConstructor(currentClass, body);
					if (localVal == null) {
						System.err.println("Constructor cannot be generated for " + currentClass.getName());
						continue;
					}
					localVarsForClasses.put(currentClass.getName(), localVal);
				}
				Local classLocal = localVarsForClasses.get(entry.getKey());
	
				// Generate the lifecycles for the different kinds of Android classes
				switch (componentType) {
				case Activity:
					generateActivityLifecycle(entry.getValue(), currentClass, endClassStmt,
							classLocal);
					break;
				case Service:
					generateServiceLifecycle(entry.getValue(), currentClass, endClassStmt,
							classLocal);
					break;
				case BroadcastReceiver:
					generateBroadcastReceiverLifecycle(entry.getValue(), currentClass, endClassStmt,
							classLocal);
					break;
				case ContentProvider:
					generateContentProviderLifecycle(entry.getValue(), currentClass, endClassStmt,
							classLocal);
					break;
				case Plain:
					// Allow the complete class to be skipped
					createIfStmt(endClassStmt);

					JNopStmt beforeClassStmt = new JNopStmt();
					body.getUnits().add(beforeClassStmt);
					for(SootMethod currentMethod : plainMethods.values()) {
						if (!currentMethod.isStatic() && classLocal == null) {
							System.out.println("Skipping method " + currentMethod + " because we have no instance");
							continue;
						}
						
						// Create a conditional call on the current method
						JNopStmt thenStmt = new JNopStmt();
						createIfStmt(thenStmt);
						buildMethodCall(currentMethod, body, classLocal, generator);
						body.getUnits().add(thenStmt);
						
						// Because we don't know the order of the custom statements,
						// we assume that you can loop arbitrarily
						createIfStmt(beforeClassStmt);
					}
					break;
				}
			}
			finally {
				body.getUnits().add(endClassStmt);
				body.getUnits().add(entryExitStmt);
			}
		}
		
		// Add conditional calls to the application callback methods
		if (applicationLocal != null)
			addApplicationCallbackMethods(applicationClass, applicationLocal);
		
		JEqExpr cond = new JEqExpr(intCounter, IntConstant.v(conditionCounter));
		conditionCounter++;
		JIfStmt outerIfStmt = new JIfStmt(cond, outerStartStmt);
		body.getUnits().add(outerIfStmt);
		
		// Add a call to application.onTerminate()
		if (applicationLocal != null)
			searchAndBuildMethod(AndroidEntryPointConstants.APPLICATION_ONTERMINATE,
					applicationClass, classMap.get(applicationClass.getName()), applicationLocal);

		if (DEBUG)
			mainMethod.getActiveBody().validate();
		System.out.println("Generated main method:\n" + body);
		return mainMethod;
	}

	/**
	 * Gets the type of component represented by the given Soot class
	 * @param currentClass The class for which to get the component type
	 * @return The component type of the given class
	 */
	private ComponentType getComponentType(SootClass currentClass) {
		// Check the type of this class
		List<SootClass> extendedClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(currentClass);
		for(SootClass sc : extendedClasses){
			if(sc.getName().equals(AndroidEntryPointConstants.ACTIVITYCLASS))
				return ComponentType.Activity;
			else if(sc.getName().equals(AndroidEntryPointConstants.SERVICECLASS))
				return ComponentType.Service;
			else if(sc.getName().equals(AndroidEntryPointConstants.BROADCASTRECEIVERCLASS))
				return ComponentType.BroadcastReceiver;
			else if(sc.getName().equals(AndroidEntryPointConstants.CONTENTPROVIDERCLASS))
				return ComponentType.ContentProvider;
		}
		return ComponentType.Plain; 
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

		// ContentProvider.onCreate() runs before everything else, even before
		// Application.onCreate(). We must thus handle it elsewhere.
		// Stmt onCreateStmt = searchAndBuildMethod(AndroidEntryPointConstants.CONTENTPROVIDER_ONCREATE, currentClass, entryPoints, classLocal);
		
		// see: http://developer.android.com/reference/android/content/ContentProvider.html
		//methods
		JNopStmt startWhileStmt = new JNopStmt();
		JNopStmt endWhileStmt = new JNopStmt();
		body.getUnits().add(startWhileStmt);
		for(SootMethod currentMethod : currentClass.getMethods()){
			if(entryPoints.contains(currentMethod.toString()) && !AndroidEntryPointConstants.getContentproviderLifecycleMethods().contains(currentMethod.getSubSignature())){
				JNopStmt thenStmt = new JNopStmt();
				createIfStmt(thenStmt);
				buildMethodCall(currentMethod, body, classLocal, generator);
				body.getUnits().add(thenStmt);
			}
		}
		addCallbackMethods(currentClass, classLocal);
		body.getUnits().add(endWhileStmt);
		createIfStmt(startWhileStmt);
		// createIfStmt(onCreateStmt);
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
				JNopStmt thenStmt = new JNopStmt();
				createIfStmt(thenStmt);
				buildMethodCall(currentMethod, body, classLocal, generator);
				body.getUnits().add(thenStmt);
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
				JNopStmt thenStmt = new JNopStmt();
				createIfStmt(thenStmt);
				buildMethodCall(currentMethod, body, classLocal, generator);
				body.getUnits().add(thenStmt);
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
				JNopStmt thenStmt = new JNopStmt();
				createIfStmt(thenStmt);
				buildMethodCall(currentMethod, body, classLocal, generator);
				body.getUnits().add(thenStmt);
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
				JNopStmt thenStmt = new JNopStmt();
				createIfStmt(thenStmt);
				buildMethodCall(currentMethod, body, classLocal, generator);
				body.getUnits().add(thenStmt);
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
	 * Adds calls to the callback methods defined in the application class
	 * @param applicationClass The class in which the user-defined application
	 * is implemented
	 * @param applicationLocal The local containing the instance of the
	 * user-defined application
	 */
	private void addApplicationCallbackMethods(SootClass applicationClass,
			Local applicationLocal) {
		// There may be multiple user classes between the class registered in
		// the manifest file and the Android system class
		for (SootClass currentClass : Scene.v().getActiveHierarchy()
				.getSuperclassesOfIncluding(applicationClass)) {
			// If we have arrived at the Android system class, we can abort the
			// search
			if (currentClass.getName().equals(AndroidEntryPointConstants.APPLICATIONCLASS))
				break;
			
			for (SootMethod method : currentClass.getMethods()) {
				// We do not consider lifecycle methods which are directly inserted
				// at their respective positions
				if (AndroidEntryPointConstants.getApplicationLifecycleMethods().contains
						(method.getSubSignature()))
					continue;
				
				// Add a conditional call to the method
				JNopStmt thenStmt = new JNopStmt();
				createIfStmt(thenStmt);
				buildMethodCall(method, body, applicationLocal, generator);	
				body.getUnits().add(thenStmt);
			}
		}
	}

	/**
	 * Generates invocation statements for all callback methods which need to
	 * be invoked during the given class' run cycle.
	 * @param currentClass The class for which we currently build the lifecycle
	 * @param parentClassLocal The local containing a reference to the class
	 * for which we are currently building the lifecycle.
	 */
	private void addCallbackMethods(SootClass currentClass, Local parentClassLocal) {
		// If no callbacks are declared for the current class, there is nothing
		// to be done here
		if (!this.callbackFunctions.containsKey(currentClass.getName()))
			return;
		
		// Get all classes in which callback methods are declared
		Map<SootClass, Set<SootMethod>> callbackClasses = new HashMap<SootClass, Set<SootMethod>>();
		for (String methodSig : this.callbackFunctions.get(currentClass.getName())) {
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
			if (isCompatible(currentClass, callbackClass))
				classLocal = parentClassLocal;
			else {
				// Create a new instance of this class
				// if we need to call a constructor, we insert the respective Jimple statement here
				classLocal = generateClassConstructor(callbackClass, body, Collections.singleton(currentClass));
				if (classLocal == null) {
					System.out.println("Constructor cannot be generated for callback class "
							+ callbackClass.getName());
					continue;
				}
			}
			
			// Build the calls to all callback methods in this class
			for (SootMethod callbackMethod : callbackClasses.get(callbackClass)) {
				JNopStmt thenStmt = new JNopStmt();
				createIfStmt(thenStmt);
				buildMethodCall(callbackMethod, body, classLocal, generator);	
				body.getUnits().add(thenStmt);
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
		
		// If the method is in one of the predefined Android classes, it cannot
		// contain custom code, so we do not need to call it
		if (AndroidEntryPointConstants.isLifecycleClass(method.getDeclaringClass().getName()))
			return null;

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
	
}
