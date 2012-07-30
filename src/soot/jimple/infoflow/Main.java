package soot.jimple.infoflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.PackManager;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
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
import soot.jimple.infoflow.permissionmap.DumbSourceManager;
import soot.jimple.infoflow.permissionmap.SourceManager;
import soot.jimple.infoflow.util.ArgBuilder;
import soot.jimple.infoflow.util.ArgParser;
import soot.jimple.infoflow.util.ClassAndMethods;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.interproc.ifds.FlowFunction;
import soot.jimple.interproc.ifds.FlowFunctions;
import soot.jimple.interproc.ifds.IFDSTabulationProblem;
import soot.jimple.interproc.ifds.InterproceduralCFG;
import soot.jimple.interproc.ifds.flowfunc.Identity;
import soot.jimple.interproc.ifds.flowfunc.KillAll;
import soot.jimple.interproc.ifds.solver.IFDSSolver;
import soot.jimple.interproc.ifds.template.JimpleBasedInterproceduralCFG;
import soot.options.Options;
import soot.toolkits.scalar.Pair;

public class Main {
	static boolean debug = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
				

				final Set <Unit> initialSeeds = new HashSet<Unit>();
				
				for (SootMethod ep : Scene.v().getEntryPoints()) {
					initialSeeds.add(ep.getActiveBody().getUnits().getFirst()); //TODO: change to real initialSeeds
//					initialSeeds.put(ep, new Pair<Value, Unit>(ep.getActiveBody().getLocals().getFirst(),ep.getActiveBody().getUnits().getFirst() )
				}
				
				System.out.println(initialSeeds.size() + " " + initialSeeds.iterator().next()); //check if inserted: true
				
				final Pair<Value, Value> zeroValue = new Pair<Value, Value>(new JimpleLocal("zero", NullType.v()),null);
				
				IFDSTabulationProblem<Unit,Pair<Value, Value>,SootMethod> problem = new IFDSTabulationProblem<Unit,Pair<Value, Value>,SootMethod>() {

					public FlowFunctions<Unit, Pair<Value, Value>, SootMethod> flowFunctions() {
						return new FlowFunctions<Unit,Pair<Value, Value>,SootMethod>() {

							public FlowFunction<Pair<Value, Value>> getNormalFlowFunction(Unit src, Unit dest) {
							
								if(src instanceof AssignStmt) {
									AssignStmt assignStmt = (AssignStmt) src;
									Value right = assignStmt.getRightOp();
									Value left = assignStmt.getLeftOp();
							
									if(debug){
										System.out.println("right ("+ right.getType() +") Class is: "+right.getClass().toString());
										System.out.println("left ("+ assignStmt.getLeftOp() +") Class is: "+assignStmt.getLeftOp().getClass().toString());
									}
									//FIXME: easy solution: take whole array instead of position:
									if(left instanceof ArrayRef){
										left = ((ArrayRef)left).getBase();
									}
									
									
									if(right instanceof Value && left instanceof Value) {
										final Value leftValue =  left;
										final Value rightValue = right;
										
										return new FlowFunction<Pair<Value, Value>>() {
											
											public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
												boolean addLeftValue = false;
												
												//check if new infoflow is created here? Not necessary because this covers only calls of methods in the same class,
												//which should not be source methods (not part of android api)
												if(rightValue instanceof JVirtualInvokeExpr){
													JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) rightValue;
													SourceManager sourceManager = new DumbSourceManager(); 
													if(sourceManager.isSourceMethod(invokeExpr.getMethod().getClass(), invokeExpr.getMethodRef().name())){
														Set<Pair<Value, Value>> res = new HashSet<Pair<Value, Value>>();
														if(!source.equals(zeroValue)){
															res.add(source);
														}
														res.add(new Pair<Value, Value>(leftValue, rightValue));
														return res;
													}
												 }
												
												//normal check for infoflow
												if(!source.equals(zeroValue)){
													PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
														if(source.getO1() instanceof InstanceFieldRef && rightValue instanceof InstanceFieldRef &&
															((InstanceFieldRef)rightValue).getField().getName().equals(((InstanceFieldRef)source.getO1()).getField().getName()))
															{
															Local rightBase = (Local)((InstanceFieldRef)rightValue).getBase();
															PointsToSet ptsRight = pta.reachingObjects(rightBase);
															Local sourceBase = (Local)((InstanceFieldRef)source.getO1()).getBase();
															PointsToSet ptsSource = pta.reachingObjects(sourceBase);
															if(ptsRight.hasNonEmptyIntersection(ptsSource)) {
																addLeftValue = true;
															}
														}
														if(rightValue instanceof StaticFieldRef && source.getO1() instanceof StaticFieldRef){
														StaticFieldRef rightRef = (StaticFieldRef) rightValue;
														StaticFieldRef sourceRef = (StaticFieldRef) source.getO1();
														if(rightRef.getFieldRef().name().equals(sourceRef.getFieldRef().name()) &&
																rightRef.getFieldRef().declaringClass().equals(sourceRef.getFieldRef().declaringClass())){
															addLeftValue = true;
														}
													}
												
												
													if(rightValue instanceof ArrayRef){ 
														Local rightBase = (Local)((ArrayRef)rightValue).getBase();
														if(source.getO1().equals(rightBase) || 
															(source.getO1() instanceof Local && pta.reachingObjects(rightBase).hasNonEmptyIntersection(pta.reachingObjects((Local)source.getO1())))) { 
															addLeftValue = true;
														}
													}
													if(rightValue instanceof JCastExpr){
														if(source.getO1().equals(((JCastExpr) rightValue).getOpBox().getValue())) {
															addLeftValue = true;
													 	}
													}
												//TODO: Modifiers der Methode überprüfen?
													if(rightValue instanceof JVirtualInvokeExpr){
														JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) rightValue;
														System.out
																.println(invokeExpr.getMethodRef().name());
														System.out
															.println(invokeExpr.getMethod().isNative());
														System.out.println(invokeExpr.getMethod().getModifiers());
														System.out.println(soot.Modifier.isNative(invokeExpr.getMethod().getModifiers()));
													
													 	if(((JVirtualInvokeExpr)rightValue).getMethodRef().name().equals("clone") ||
															 ((JVirtualInvokeExpr)rightValue).getMethodRef().name().equals("concat") ){ //TODO: sonderfall, welche noch?
														 if(source.getO1().equals(((JVirtualInvokeExpr) rightValue).getBaseBox().getValue())) {
																addLeftValue = true;
															}
													 	}
													}
												
													//generic case, is true for Locals, ArrayRefs that are equal etc..
													if(source.getO1().equals(rightValue)) {
														addLeftValue = true;
													}
												
													//if one of them is true -> add leftValue
													if(addLeftValue){
														Set<Pair<Value, Value>> res = new HashSet<Pair<Value, Value>>();
														res.add(source);
														res.add(new Pair<Value, Value>(leftValue, source.getO2()));
														return res;
													}
												}
												return Collections.singleton(source);

											}
										};
									}
																		
								}
								return Identity.v();
							}

							public FlowFunction<Pair<Value, Value>> getCallFlowFunction(Unit src, final SootMethod dest) {
				
								final Stmt stmt = (Stmt) src;
								
								final InvokeExpr ie = stmt.getInvokeExpr();
								System.out.println("Call "+ ie);
								final List<Value> callArgs = ie.getArgs();
								final List<Value> paramLocals = new ArrayList<Value>();
								for(int i=0;i<dest.getParameterCount();i++) {
									paramLocals.add(dest.getActiveBody().getParameterLocal(i));
									if(debug)
										System.out.println("Param in Procedure: " + paramLocals.get(i));
								}
								return new FlowFunction<Pair<Value, Value>>() {

									public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
										
										int argIndex = callArgs.indexOf(source.getO1());
										if(debug)
											System.out.println("Variable used for Param: "+argIndex + " " + source);
										if(argIndex>-1) {
											Set<Pair<Value, Value>> res = new HashSet<Pair<Value, Value>>();
											res.add(new Pair<Value, Value>(paramLocals.get(argIndex), source.getO2()));
											return res;
										}
										return Collections.emptySet();
									}
								};
							}

							public FlowFunction<Pair<Value, Value>> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt, Unit retSite) {
								if (exitStmt instanceof ReturnStmt) {								
									ReturnStmt returnStmt = (ReturnStmt) exitStmt;
									Value op = returnStmt.getOp();
									if(op instanceof Value) {
										if(callSite instanceof DefinitionStmt) {
											DefinitionStmt defnStmt = (DefinitionStmt) callSite;
											Value leftOp = defnStmt.getLeftOp();
											final Value tgtLocal =  leftOp;
											final Value retLocal =  op;
											return new FlowFunction<Pair<Value, Value>>() {

												public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
													if(source.getO1()==retLocal)
														return Collections.singleton(new Pair<Value, Value>(tgtLocal, source.getO2()));
													return Collections.emptySet();
												}
													
											};
										}
										
									}
								} 
								return KillAll.v();
							}

							public FlowFunction<Pair<Value, Value>> getCallToReturnFlowFunction(Unit call, Unit returnSite) {
								//obviously we have to check for the correct class, too (not only method)
								SourceManager sourceManager = new DumbSourceManager(); 
								if(call instanceof JAssignStmt){
									final JAssignStmt stmt = (JAssignStmt) call;
									if(sourceManager.isSourceMethod(stmt.getInvokeExpr().getMethod().getClass(),stmt.getInvokeExpr().getMethodRef().name())){
										return new FlowFunction<Pair<Value,Value>>() {

											@Override
											public Set<Pair<Value, Value>> computeTargets(
													Pair<Value, Value> source) {
												Set<Pair<Value, Value>> res = new HashSet<Pair<Value, Value>>();
												res.add(source);
												res.add(new Pair<Value, Value>(stmt.getLeftOp(), stmt.getInvokeExpr()));
												return res;
											}
										};
									}
								
								}
								return Identity.v();
							}
						};						
					}

					public InterproceduralCFG<Unit,SootMethod> interproceduralCFG() {
						return new JimpleBasedInterproceduralCFG();
					}

					

					public Pair<Value, Value> zeroValue() {
						return zeroValue;
					}

					@Override
					public Set<Unit> initialSeeds() {
						return initialSeeds;
						
					}
				};
				
				
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
						System.err.println(l.getO1());
					}
				}
				
			}
		}));
		
		ArgParser parser = new ArgParser();
		ArgBuilder builder = new ArgBuilder();
		ClassAndMethods classmethods = null;
		String[] newArgs = null;
		if(args.length>0){
			if(Arrays.asList(args).contains(ArgParser.CLASSKEYWORD)){
				classmethods = parser.parseClassArguments(args);
				newArgs = builder.buildArgs(classmethods.getClassName());
			} else if (Arrays.asList(args).contains(ArgParser.ANDROIDKEYWORD)){
				//TODO: to be added by bachelor thesis
			}else{
				//just use normal args and provide default testclass
				classmethods = new ClassAndMethods();
				classmethods.setClassName("Test");
				classmethods.addMethodName("main");
				newArgs = args;
			}
		}

		Options.v().parse(newArgs);
		SootClass c = Scene.v().forceResolve(classmethods.getClassName(), SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		c.setApplicationClass();
		List<SootMethod> entryPoints = new ArrayList<SootMethod>();
		for(String methodname : classmethods.getMethodNames()){
			SootMethod method1 = c.getMethodByName(methodname);
			entryPoints.add(method1);
		}
		
		Scene.v().setEntryPoints(entryPoints);
		PackManager.v().runPacks();
	}
}
