package soot.jimple.infoflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.PackManager;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.infoflow.util.ArgBuilder;
import soot.jimple.infoflow.util.ClassAndMethods;
import soot.jimple.interproc.ifds.solver.IFDSSolver;
import soot.options.Options;
import soot.toolkits.scalar.Pair;

public class Infoflow implements IInfoflow {

	@Override
	public void computeInfoflow(String classNameWithPath,
			boolean hasMainMethod, List<String> entryMethodNames) {
		//convert input:
		ClassAndMethods classmethods = new ClassAndMethods();
		classmethods.setMethodNames(entryMethodNames);
		classmethods.setClassName(classNameWithPath);
		classmethods.setNomain(!hasMainMethod);
		
		//add SceneTransformer:
		addSceneTransformer();
		
		//prepare soot arguments:
		ArgBuilder builder = new ArgBuilder();
		String [] args = builder.buildArgs(classmethods.getClassName());
		Options.v().parse(args);
		
		List<SootMethod> entryPoints;
		SootClass c = Scene.v().forceResolve(classmethods.getClassName(), SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		c.setApplicationClass();
		if(!hasMainMethod){
			entryPoints = createDummyMain(classmethods, c);
		} else{
			entryPoints = new ArrayList<SootMethod>();
			for(String methodName : classmethods.getMethodNames()){
				SootMethod method1 = c.getMethodByName(methodName);
				entryPoints.add(method1);
			}
		}
		
		Scene.v().setEntryPoints(entryPoints);
		PackManager.v().runPacks();
	}

	
	/**
	 * Soot requires a main method, so we create a dummy method which calls all entry functions.
	 * @param classmethods the methods to call
	 * @param createdClass the class which contains the methods
	 * @return list of entryPoints
	 */
	public List<SootMethod> createDummyMain(ClassAndMethods classmethods, SootClass createdClass){
		
		
		List<SootMethod> entryPoints = new ArrayList<SootMethod>();
		JimpleBody body = Jimple.v().newBody();
		LocalGenerator generator = new LocalGenerator(body);
		Local tempLocal = generator.generateLocal(RefType.v(classmethods.getClassName()));
		NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(classmethods.getClassName()));
		AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, newExpr);			
		SpecialInvokeExpr sinvokeExpr = Jimple.v().newSpecialInvokeExpr(tempLocal, Scene.v().makeMethodRef(createdClass, "<init>", new ArrayList<Type>(), VoidType.v(), false));
		body.getUnits().add(assignStmt);
		body.getUnits().add(Jimple.v().newInvokeStmt(sinvokeExpr));
		
		for(String methodName : classmethods.getMethodNames()){
			Local stringLocal = generator.generateLocal(RefType.v("java.lang.String"));
			SootMethod m = new SootMethod(methodName, new ArrayList<Type>(), RefType.v("java.lang.String"));
			m.setDeclaringClass(createdClass);
			VirtualInvokeExpr vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, m.makeRef());
			//original: body.getUnits().add(Jimple.v().newInvokeStmt(vInvokeExpr));
			AssignStmt assignStmt2 = Jimple.v().newAssignStmt(stringLocal, vInvokeExpr);
			body.getUnits().add(assignStmt2);
		}
		
		SootMethod mainMethod = new SootMethod("dummyMainMethod", new ArrayList<Type>(), VoidType.v());
		body.setMethod(mainMethod);
		mainMethod.setActiveBody(body);
		SootClass mainClass = new SootClass("dummyMainClass");
		mainClass.setApplicationClass();
		mainClass.addMethod(mainMethod);
		Scene.v().addClass(mainClass);
		entryPoints.add(mainMethod);
		return entryPoints;
	}
	
	public void addSceneTransformer(){
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				
				InfoflowProblem problem = new InfoflowProblem();
				for (SootMethod ep : Scene.v().getEntryPoints()) {
					problem.initialSeeds.add(ep.getActiveBody().getUnits().getFirst()); //TODO: change to real initialSeeds
				}
				
				IFDSSolver<Unit,Pair<Value, Value>,SootMethod> solver = new IFDSSolver<Unit,Pair<Value, Value>,SootMethod>(problem);	
				solver.solve();
				
				for(SootMethod ep : Scene.v().getEntryPoints()) {
					Unit ret = ep.getActiveBody().getUnits().getLast();
					
					System.err.println(ep.getActiveBody());
					
					System.err.println("----------------------------------------------");
					System.err.println("At end of: "+ep.getSignature());
					System.err.println("Variables:");
					System.err.println("----------------------------------------------");
					
					for(Pair<Value, Value> l: solver.ifdsResultsAt(ret)) {
						System.err.println(l.getO1() + " contains value from " + l.getO2());
					}
				}
				
			}
		}));
	}
	
}
