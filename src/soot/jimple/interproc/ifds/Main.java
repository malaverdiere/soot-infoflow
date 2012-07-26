package soot.jimple.interproc.ifds;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.interproc.ifds.flowfunc.Identity;
import soot.jimple.interproc.ifds.flowfunc.KillAll;
import soot.jimple.interproc.ifds.own.APKAnalyzer;
import soot.jimple.interproc.ifds.solver.IFDSSolver;
import soot.jimple.interproc.ifds.template.JimpleBasedInterproceduralCFG;
import soot.toolkits.scalar.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class Main {
	static boolean debug = false;
	static boolean android = false;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				for(SootMethod m: Scene.v().getMainClass().getMethods()) {
					if(m.hasActiveBody())
						System.err.println(m.getActiveBody());
				}

				
				Iterator<SootClass> classIterator = Scene.v().getClasses().iterator();
				while(classIterator.hasNext()){
					SootClass current = classIterator.next();
					System.out.println(current.getName() + " has the following methods:");
					for(SootMethod method : current.getMethods()){
						System.out.println(method.getName());
					}
				}
				
				final Multimap<SootMethod,Pair<Value, Unit>> initialSeeds = HashMultimap.create();
				initialSeeds.put(Scene.v().getMainMethod(), new Pair<Value, Unit>(Scene.v().getMainMethod().getActiveBody().getLocals().getFirst(), Scene.v().getMainMethod().getActiveBody().getUnits().getFirst()));
				
				final Pair<Value, Unit> zeroValue = new Pair<Value, Unit>(new JimpleLocal("zero", NullType.v()),null);
				final SourceFinder sFinder = new SourceFinder();
				
				IFDSTabulationProblem<Unit,Pair<Value, Unit>,SootMethod> problem = new IFDSTabulationProblem<Unit,Pair<Value, Unit>,SootMethod>() {

					public FlowFunctions<Unit, Pair<Value, Unit>, SootMethod> flowFunctions() {
						return new FlowFunctions<Unit,Pair<Value, Unit>,SootMethod>() {

							public FlowFunction<Pair<Value, Unit>> getNormalFlowFunction(Unit src, Unit dest) {
							
								if(src instanceof AssignStmt) {
									AssignStmt assignStmt = (AssignStmt) src;
									Value right = assignStmt.getRightOp();
									Value left = assignStmt.getLeftOp();
									
									final Unit sourceUnit = sFinder.getSourceOrNull(assignStmt);
									if(sourceUnit != null){
										//TODO: add new with unit - no need for further checks..
										//return
									}
									if(debug){
										System.out.println("right ("+ right.getType() +") Class is: "+right.getClass().toString());
										System.out.println("left ("+ assignStmt.getLeftOp() +") Class is: "+assignStmt.getLeftOp().getClass().toString());
									}
									//FIXME: easy solution: take whole array instead of position:
									if(left instanceof ArrayRef){
										left = ((ArrayRef)left).getBase();
									}
									if(left instanceof Local && ((Local)left).getName().equals("y3498")){
										System.out.println("salat");
									}
									
									if(right instanceof Value && left instanceof Value) {
										final Value leftValue =  left;
										final Value rightValue = right;
										return new FlowFunction<Pair<Value, Unit>>() {
											
											public Set<Pair<Value, Unit>> computeTargets(Pair<Value, Unit> source) {
												boolean addLeftValue = false;
//												if(rightValue instanceof InstanceFieldRef && source.getO1() instanceof InstanceFieldRef && ((Local)((InstanceFieldRef)rightValue).getBase()).getName().equals("testo3")){
//													System.out
//															.println("c123 " + Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)rightValue).getBase()).hasNonEmptyIntersection(Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)source.getO1()).getBase())));
//												}
												
												if(source.getO1() instanceof InstanceFieldRef && rightValue instanceof InstanceFieldRef &&
														((InstanceFieldRef)rightValue).getField().getName().equals(((InstanceFieldRef)source.getO1()).getField().getName()) &&
														Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)rightValue).getBase()).hasNonEmptyIntersection(Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)source.getO1()).getBase())))
														{
													System.out
															.println("-------start------");
													System.out
															.println(Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)rightValue).getBase()).hasNonEmptyIntersection(Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)source.getO1()).getBase())));
													System.out
															.println("ptset von rightValue: " + Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)rightValue).getBase()).toString());
													System.out
													.println("ptset von src: " + Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)source.getO1()).getBase()).toString());
											
													Set<Type> x=Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)rightValue).getBase()).possibleTypes();
													for(Type t : x){
														System.out
																.println(t.getClass() + " " + t.toString());
													}
													Set<Type> sourceT=Scene.v().getPointsToAnalysis().reachingObjects((Local)((InstanceFieldRef)source.getO1()).getBase()).possibleTypes();
													for(Type t : sourceT){
														System.out
																.println(t.getClass() + " " + t.toString());
													}
													System.out
															.println("---------end-------");
													addLeftValue = true;
												}
												if(rightValue instanceof StaticFieldRef && source.getO1() instanceof StaticFieldRef){
													StaticFieldRef rightRef = (StaticFieldRef) rightValue;
													StaticFieldRef sourceRef = (StaticFieldRef) source.getO1();
													if(rightRef.getFieldRef().name().equals(sourceRef.getFieldRef().name()) &&
															rightRef.getFieldRef().declaringClass().equals(sourceRef.getFieldRef().declaringClass())){
														addLeftValue = true;
													}
												}
												
												
												if(rightValue instanceof ArrayRef && (source.getO1().equals(((ArrayRef)rightValue).getBase()) || 
														(source.getO1() instanceof Local && Scene.v().getPointsToAnalysis().reachingObjects((Local)((ArrayRef)rightValue).getBase()).hasNonEmptyIntersection(Scene.v().getPointsToAnalysis().reachingObjects((Local)source.getO1()))))) { 
													addLeftValue = true;
												}
												if(rightValue instanceof JCastExpr){
													 if(source.getO1().equals(((JCastExpr) rightValue).getOpBox().getValue())) {
													 addLeftValue = true;
													 }
												}
												//TODO: Modifiers der Methode überprüfen?
												
												if(rightValue instanceof JVirtualInvokeExpr){
													
													JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) rightValue;
													if(((Local)invokeExpr.getBaseBox().getValue()).getName().contains("z100gh")){
														System.out
																.println("Alarm");
													}
													System.out
															.println(invokeExpr.getMethod().isNative());
													System.out.println(invokeExpr.getMethod().getModifiers());
													System.out.println(soot.Modifier.isNative(invokeExpr.getMethod().getModifiers()));
													
													 if(((JVirtualInvokeExpr)rightValue).getMethodRef().name().equals("clone") ||
															 ((JVirtualInvokeExpr)rightValue).getMethodRef().name().equals("concat") ){ //TODO: sonderfall, welche noch?
														 if(source.getO1().equals(((JVirtualInvokeExpr) rightValue).getBaseBox().getValue())) {
																addLeftValue = true;
															}
															//return Collections.singleton(source);
													 }
												}
												
												//generic case, is true for Locals, ArrayRefs that are equal etc..
												if(source.getO1().equals(rightValue)) {
													addLeftValue = true;
												}
												
												//if one of them is true -> add leftValue
												if(addLeftValue){
													Set<Pair<Value, Unit>> res = new HashSet<Pair<Value, Unit>>();
													res.add(source);
													res.add(new Pair<Value, Unit>(leftValue, source.getO2()));
													return res;
												}
												return Collections.singleton(source);
											}
										};
									}
																		
								}
								return Identity.v();
							}

							public FlowFunction<Pair<Value, Unit>> getCallFlowFunction(Unit src, final SootMethod dest) {
								
								Stmt stmt = (Stmt) src;
								InvokeExpr ie = stmt.getInvokeExpr();
								
								final List<Value> callArgs = ie.getArgs();
								final List<Value> paramLocals = new ArrayList<Value>();
								for(int i=0;i<dest.getParameterCount();i++) {
									paramLocals.add(dest.getActiveBody().getParameterLocal(i));
									if(debug)
										System.out.println("Param in Procedure: " + paramLocals.get(i));
								}
								return new FlowFunction<Pair<Value, Unit>>() {

									public Set<Pair<Value, Unit>> computeTargets(Pair<Value, Unit> source) {
										int argIndex = callArgs.indexOf(source.getO1());
										if(debug)
											System.out.println("Variable used for Param: "+argIndex + " " + source);
										if(argIndex>-1) {
											Set<Pair<Value, Unit>> res = new HashSet<Pair<Value, Unit>>();
											res.add(new Pair<Value, Unit>(paramLocals.get(argIndex), source.getO2()));
											return res;
										}
										return Collections.emptySet();
									}
								};
							}

							public FlowFunction<Pair<Value, Unit>> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt, Unit retSite) {
								if (exitStmt instanceof ReturnStmt) {								
									ReturnStmt returnStmt = (ReturnStmt) exitStmt;
									Value op = returnStmt.getOp();
									if(op instanceof Value) {
										if(callSite instanceof DefinitionStmt) {
											DefinitionStmt defnStmt = (DefinitionStmt) callSite;
											Value leftOp = defnStmt.getLeftOp();
											final Value tgtLocal =  leftOp;
											final Value retLocal =  op;
											return new FlowFunction<Pair<Value, Unit>>() {

												public Set<Pair<Value, Unit>> computeTargets(Pair<Value, Unit> source) {
													if(source.getO1()==retLocal)
														return Collections.singleton(new Pair<Value, Unit>(tgtLocal, source.getO2())); //TODO: is this correct?
													return Collections.emptySet();
												}
													
											};
										}
										
									}
								} 
								return KillAll.v();
							}

							public FlowFunction<Pair<Value, Unit>> getCallToReturnFlowFunction(Unit call, Unit returnSite) {
								return Identity.v();
							}
						};						
					}

					public InterproceduralCFG<Unit,SootMethod> interproceduralCFG() {
						return new JimpleBasedInterproceduralCFG();
					}

					public Multimap<SootMethod, Pair<Value, Unit>> initialSeeds() {
						return initialSeeds;
					}

					public Pair<Value, Unit> zeroValue() {
						return zeroValue;
					}
				};
				
				
				IFDSSolver<Unit,Pair<Value, Unit>,SootMethod> solver = new IFDSSolver<Unit,Pair<Value, Unit>,SootMethod>(problem);	
				solver.solve();
				Unit ret = Scene.v().getMainMethod().getActiveBody().getUnits().getLast();
				
				System.err.println("----------------------------------------------");
				System.err.println("Variables:");
				System.err.println("----------------------------------------------");
				
				for(Pair<Value, Unit> l: solver.ifdsResultsAt(ret)) {
					System.err.println(l.getO1());
				}
			}
		}));
		
		//----------------- read EntryPoints begin
		List<String> argList = Arrays.asList(args);
		//Todo: make it more insensitive against capital letters (not -toLowerCase on the array or on ArraytoList)
		if(argList.contains("-cp") && argList.indexOf("-cp")+1 < argList.size()){
			String cpArg = argList.get(argList.indexOf("-cp")+1);
			APKAnalyzer analyzer = new APKAnalyzer();
			while(cpArg.contains(";")){
				
			}
			if(cpArg.endsWith(".apk")){
				File apkFile = new File(cpArg);
				if(apkFile.exists()){
					List<String> list = analyzer.getActivities(apkFile);
					for(String current : list){
						System.out.println(current);//TODO: insert in entryPoints
					}
				}
			}
		}
		
		//----------------- read EntryPoints end
		boolean nomain = false;
		if(nomain){
			//Scene.v().addBasicClass("TestNoMain", SootClass.SIGNATURES);
			//Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);
			//Scene.v().addBasicClass("java.util.Locale",SootClass.SIGNATURES);
			//Scene.v().addBasicClass("java.lang.System",SootClass.BODIES);
			SootClass c = Scene.v().forceResolve("TestNoMain", SootClass.BODIES);
			Scene.v().loadBasicClasses();
			Scene.v().loadNecessaryClasses();
			//SootClass c = Scene.v().loadClass("TestNoMain", 1);
			//SootClass c = Scene.v().loadClassAndSupport("TestNoMain");
			c.setApplicationClass();
			SootMethod method1 = c.getMethodByName("onCreate");
			List<SootMethod> entryPoints = new ArrayList<SootMethod>();
			entryPoints.add(method1);
			Scene.v().setEntryPoints(entryPoints);
		}
			
		soot.Main.main(args);
	}
	

}

//only for test:
//Iterator<SootClass> classIterator = Scene.v().getClasses().iterator();
//while(classIterator.hasNext()){
//	SootClass current = classIterator.next();
//	System.out.println(current.getName() + " has the following methods:");
//	for(SootMethod method : current.getMethods()){
//		System.out.println(method.getName());
//	}
//}
