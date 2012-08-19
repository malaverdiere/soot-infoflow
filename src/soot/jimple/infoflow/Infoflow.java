package soot.jimple.infoflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.interproc.ifds.InterproceduralCFG;
import soot.jimple.interproc.ifds.solver.IFDSSolver;
import soot.options.Options;
import soot.toolkits.scalar.Pair;

public class Infoflow implements IInfoflow {

	final List<SootMethod> originalEntryPoints = new ArrayList<SootMethod>();
	
	@Override
	public void computeInfoflow(String path, List<String> entryPoints, List<String> sources, List<String> sinks) {
		//convert to internal format:
		SootMethodRepresentationParser parser = new SootMethodRepresentationParser();
		HashMap<String, List<SootMethod>> entryPointMap = parser.parseMethodList(entryPoints);
		//add SceneTransformer:
		addSceneTransformer();
		
		for(Entry<String, List<SootMethod>> classEntry : entryPointMap.entrySet()){
			//prepare soot arguments:
			ArgBuilder builder = new ArgBuilder();
			String [] args = builder.buildArgs(classEntry.getKey());
			Options.v().set_debug(true);
			Options.v().parse(args);
		
			//entryPoints are the entryPoints required by Soot to calculate Graph - if there is no main method, we have to create a new main method and use it as entryPoint, but store our real entryPoints
			List<SootMethod> sootEntryPoints;
			SootClass c = Scene.v().forceResolve(classEntry.getKey(), SootClass.BODIES);
			Scene.v().loadNecessaryClasses();
			c.setApplicationClass();
			//FIXME: call this always? can't check if there is a main method or not?
			sootEntryPoints = createDummyMain(classEntry, c);
			
			for(SootMethod method : classEntry.getValue()){
				SootMethod methodWithBody = c.getMethodByName(method.getName());
				originalEntryPoints.add(methodWithBody);
				sootEntryPoints.add(methodWithBody);
				
			}
			
		
			Scene.v().setEntryPoints(sootEntryPoints);
			PackManager.v().runPacks();
		}
		
	}
	
	/**
	 * Soot requires a main method, so we create a dummy method which calls all entry functions.
	 * @param classmethods the methods to call
	 * @param createdClass the class which contains the methods
	 * @return list of entryPoints
	 */
	public List<SootMethod> createDummyMain(Entry<String, List<SootMethod>> classEntry, SootClass createdClass){
		
		
		List<SootMethod> entryPoints = new ArrayList<SootMethod>();
		JimpleBody body = Jimple.v().newBody();
		LocalGenerator generator = new LocalGenerator(body);
		Local tempLocal = generator.generateLocal(RefType.v(classEntry.getKey()));
		NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(classEntry.getKey()));
		AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, newExpr);			
		SpecialInvokeExpr sinvokeExpr = Jimple.v().newSpecialInvokeExpr(tempLocal, Scene.v().makeMethodRef(createdClass, "<init>", new ArrayList<Type>(), VoidType.v(), false));
		body.getUnits().add(assignStmt);
		body.getUnits().add(Jimple.v().newInvokeStmt(sinvokeExpr));
		
		for(SootMethod method : classEntry.getValue()){
			Local stringLocal = null;
			method.setDeclaringClass(createdClass);
			VirtualInvokeExpr vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, method.makeRef());
			if(!(method.getReturnType() instanceof VoidType)){
				stringLocal = generator.generateLocal(method.getReturnType());
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
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				
			InfoflowProblem problem = new InfoflowProblem();
			for (SootMethod ep : Scene.v().getEntryPoints()) {
				problem.initialSeeds.add(ep.getActiveBody().getUnits().getFirst()); //TODO: change to real initialSeeds
			}
			
			IFDSSolver<Unit,Pair<Value, Value>,SootMethod, InterproceduralCFG<Unit, SootMethod>> solver = new IFDSSolver<Unit,Pair<Value, Value>,SootMethod, InterproceduralCFG<Unit, SootMethod>>(problem);	

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
		});
		if(PackManager.v().getPack("wjtp").get("wjtp.ifds") == null){
			PackManager.v().getPack("wjtp").add(transform);
		} else{
			//TODO starting junit tests, we have to delete the Pack from the previous pack
			Iterator it = PackManager.v().getPack("wjtp").iterator();
			while(it.hasNext()){
				Object current = it.next();
				if(current instanceof Transform && ((Transform)current).getPhaseName().equals("wjtp.ifds")){
					it.remove();
					break;
				}
				
			}
			PackManager.v().getPack("wjtp").add(transform);
		}
	}
	
}

