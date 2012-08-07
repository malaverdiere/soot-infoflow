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
import soot.jimple.infoflow.data.AnalyzeClass;
import soot.jimple.infoflow.data.AnalyzeMethod;
import soot.jimple.infoflow.util.ArgBuilder;
import soot.jimple.interproc.ifds.solver.IFDSSolver;
import soot.options.Options;
import soot.toolkits.scalar.Pair;

public class Infoflow implements IInfoflow {

	@Override
	public void computeInfoflow(String path, List<AnalyzeClass> classes, List<String> sources, List<String> sinks) {
		//add SceneTransformer:
		addSceneTransformer();
		
		for(AnalyzeClass currentClass : classes){
			//prepare soot arguments:
			ArgBuilder builder = new ArgBuilder();
			String [] args = builder.buildArgs(currentClass.getNameWithPath());
			Options.v().parse(args);
		
			List<SootMethod> entryPoints;
			SootClass c = Scene.v().forceResolve(currentClass.getNameWithPath(), SootClass.BODIES);
			Scene.v().loadNecessaryClasses();
			c.setApplicationClass();
			if(!currentClass.hasMain()){
				entryPoints = createDummyMain(currentClass, c);
			} else{
				entryPoints = new ArrayList<SootMethod>();
				for(AnalyzeMethod method : currentClass.getMethods()){
					SootMethod method1 = c.getMethodByName(method.getName());
					entryPoints.add(method1);
				}
			}
		
			Scene.v().setEntryPoints(entryPoints);
			PackManager.v().runPacks();
		}
		
	}
	
	/**
	 * Soot requires a main method, so we create a dummy method which calls all entry functions.
	 * @param classmethods the methods to call
	 * @param createdClass the class which contains the methods
	 * @return list of entryPoints
	 */
	public List<SootMethod> createDummyMain(AnalyzeClass theClass, SootClass createdClass){
		
		
		List<SootMethod> entryPoints = new ArrayList<SootMethod>();
		JimpleBody body = Jimple.v().newBody();
		LocalGenerator generator = new LocalGenerator(body);
		Local tempLocal = generator.generateLocal(RefType.v(theClass.getNameWithPath()));
		NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(theClass.getNameWithPath()));
		AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, newExpr);			
		SpecialInvokeExpr sinvokeExpr = Jimple.v().newSpecialInvokeExpr(tempLocal, Scene.v().makeMethodRef(createdClass, "<init>", new ArrayList<Type>(), VoidType.v(), false));
		body.getUnits().add(assignStmt);
		body.getUnits().add(Jimple.v().newInvokeStmt(sinvokeExpr));
		
		for(AnalyzeMethod method : theClass.getMethods()){
			SootMethod sootMethod;
			Local stringLocal = null;
			ArrayList<Type> paramTypes = new ArrayList<Type>();
			if(method.getParameters() != null){
				for(String paramType : method.getParameters()){
					paramTypes.add(RefType.v(paramType));
				}
			}
			if(method.getReturnType() != null && !method.getReturnType().equals("")){
				stringLocal = generator.generateLocal(RefType.v(method.getReturnType()));
				sootMethod = new SootMethod(method.getName(), paramTypes, RefType.v(method.getReturnType()));
			} else {
				//no returnType:
				sootMethod = new SootMethod(method.getName(), paramTypes, VoidType.v());
			}
			sootMethod.setDeclaringClass(createdClass);
			VirtualInvokeExpr vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, sootMethod.makeRef());
			if(method.getReturnType() != null && !method.getReturnType().equals("")){
				AssignStmt assignStmt2 = Jimple.v().newAssignStmt(stringLocal, vInvokeExpr);
				body.getUnits().add(assignStmt2);
			}else{
				body.getUnits().add(Jimple.v().newInvokeStmt(vInvokeExpr));
			}
			
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

