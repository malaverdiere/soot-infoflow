package soot.jimple.infoflow;

import java.util.ArrayList;
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
import soot.jimple.internal.JCastExpr;
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
					System.out.println("# " +ep.getActiveBody().getUnits().getSuccOf(ep.getActiveBody().getUnits().getFirst()) + " * " + ep.getActiveBody().getLocals().getSuccOf(ep.getActiveBody().getLocals().getFirst()));
					initialSeeds.add(ep.getActiveBody().getUnits().getFirst()); //TODO: change to real initialSeeds
//					initialSeeds.put(ep, new Pair<Value, Unit>(ep.getActiveBody().getLocals().getFirst(),ep.getActiveBody().getUnits().getFirst() )
				}
				
				
				final Pair<Value, Unit> zeroValue = new Pair<Value, Unit>(new JimpleLocal("zero", NullType.v()),null);
				
				IFDSTabulationProblem<Unit,Pair<Value, Unit>,SootMethod> problem = new IFDSTabulationProblem<Unit,Pair<Value, Unit>,SootMethod>() {

					public FlowFunctions<Unit, Pair<Value, Unit>, SootMethod> flowFunctions() {
						return new FlowFunctions<Unit,Pair<Value, Unit>,SootMethod>() {

							public FlowFunction<Pair<Value, Unit>> getNormalFlowFunction(Unit src, Unit dest) {
							
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
										return new FlowFunction<Pair<Value, Unit>>() {
											
											public Set<Pair<Value, Unit>> computeTargets(Pair<Value, Unit> source) {
												boolean addLeftValue = false;
												
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
												
												
												if(rightValue instanceof ArrayRef && (source.getO1().equals(((ArrayRef)rightValue).getBase()) || 
														(source.getO1() instanceof Local && pta.reachingObjects((Local)((ArrayRef)rightValue).getBase()).hasNonEmptyIntersection(pta.reachingObjects((Local)source.getO1()))))) { 
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
														return Collections.singleton(new Pair<Value, Unit>(tgtLocal, source.getO2()));
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

					

					public Pair<Value, Unit> zeroValue() {
						return zeroValue;
					}

					@Override
					public Set<Unit> initialSeeds() {
						return initialSeeds;
						
					}
				};
				
				
				IFDSSolver<Unit,Pair<Value, Unit>,SootMethod> solver = new IFDSSolver<Unit,Pair<Value, Unit>,SootMethod>(problem);	
				solver.solve();
				
				for(SootMethod ep : Scene.v().getEntryPoints()) {
					Unit ret = ep.getActiveBody().getUnits().getLast();
					
					System.err.println(ep.getActiveBody());
					
					System.err.println("----------------------------------------------");
					System.err.println("At end of: "+ep.getSignature());
					System.err.println("Variables:");
					System.err.println("----------------------------------------------");
					
					for(Pair<Value, Unit> l: solver.ifdsResultsAt(ret)) {
						System.err.println(l.getO1());
					}
				}
				
			}
		}));
		

	
		Options.v().parse(args);
			
		SootClass c = Scene.v().forceResolve("TestNoMain", SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		c.setApplicationClass();
		SootMethod method1 = c.getMethodByName("onCreate");
		List<SootMethod> entryPoints = new ArrayList<SootMethod>();
		entryPoints.add(method1);
		Scene.v().setEntryPoints(entryPoints);
			
		PackManager.v().runPacks();
//			old call: soot.Main.main(args);
		
	}
}
