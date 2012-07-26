package soot.jimple.interproc.ifds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.interproc.ifds.flowfunc.Identity;
import soot.jimple.interproc.ifds.flowfunc.KillAll;
import soot.jimple.interproc.ifds.problems.IFDSReachingDefinitions;
import soot.jimple.interproc.ifds.solver.IFDSSolver;
import soot.jimple.interproc.ifds.template.JimpleBasedInterproceduralCFG;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class CopyOfMain {

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

				
				final Multimap<SootMethod,Local> initialSeeds = HashMultimap.create();
				initialSeeds.put(Scene.v().getMainMethod(), Scene.v().getMainMethod().getActiveBody().getLocals().getFirst());
				
				final Local zeroValue = new JimpleLocal("zero", NullType.v());
				
				IFDSTabulationProblem<Unit,Local,SootMethod> problem = new IFDSTabulationProblem<Unit,Local,SootMethod>() {

					public FlowFunctions<Unit, Local, SootMethod> flowFunctions() {
						return new FlowFunctions<Unit,Local,SootMethod>() {

							public FlowFunction<Local> getNormalFlowFunction(Unit src, Unit dest) {
								if(src instanceof AssignStmt) {
									AssignStmt assignStmt = (AssignStmt) src;
									Value right = assignStmt.getRightOp();
									System.out.println("right ("+ right.getType() +") Class is: "+right.getClass().toString());
									System.out.println("left ("+ assignStmt.getLeftOp() +") Class is: "+assignStmt.getLeftOp().getClass().toString());
									// simple case: Local assignment:
									if(right instanceof Local && assignStmt.getLeftOp() instanceof Local) {
										final Local rightLocal = (Local) right;
										final Local leftLocal = (Local) assignStmt.getLeftOp();
										return new FlowFunction<Local>() {
											
											public Set<Local> computeTargets(Local source) {
												if(source.equals(rightLocal)) {
													Set<Local> res = new HashSet<Local>();
													res.add(source);
													res.add(leftLocal);
													return res;
												}
												return Collections.singleton(source);
											}
										};
									}
									//TODO: what about "mixed" cases: right local, left global etc...
									if(right instanceof StaticFieldRef && assignStmt.getLeftOp() instanceof Local) {
										final StaticFieldRef rightRef = (StaticFieldRef) right;
										final Local leftLocal = (Local) assignStmt.getLeftOp();
										return new FlowFunction<Local>() {
											
											public Set<Local> computeTargets(Local source) {
												if(source.equals(rightRef)) {
													Set<Local> res = new HashSet<Local>();
													res.add(source);
													res.add(leftLocal);
													return res;
												}
												return Collections.singleton(source);
											}
										};
									}
//									if(right instanceof Local && assignStmt.getLeftOp() instanceof StaticFieldRef) {
//										final Local rightLocal = (Local) right;
//										final StaticFieldRef leftLocal = (StaticFieldRef) assignStmt.getLeftOp();
//										return new FlowFunction<Local>() {
//											
//											public Set<Local> computeTargets(Local source) {
//												if(source.equals(rightLocal)) {
//													Set<Local> res = new HashSet<Local>();
//													res.add(source);
//													res.add(leftLocal);
//													return res;
//												}
//												return Collections.singleton(source);
//											}
//										};
									//}
									
								}
								return Identity.v();
							}

							public FlowFunction<Local> getCallFlowFunction(Unit src, final SootMethod dest) {
								Stmt stmt = (Stmt) src;
								InvokeExpr ie = stmt.getInvokeExpr();
								final List<Value> callArgs = ie.getArgs();
								final List<Local> paramLocals = new ArrayList<Local>();
								for(int i=0;i<dest.getParameterCount();i++) {
									paramLocals.add(dest.getActiveBody().getParameterLocal(i));
								}
								return new FlowFunction<Local>() {

									public Set<Local> computeTargets(Local source) {
										int argIndex = callArgs.indexOf(source);
										if(argIndex>-1) {
											Set<Local> res = new HashSet<Local>();
											res.add(paramLocals.get(argIndex));
											return res;
										}
										return Collections.emptySet();
									}
								};
							}

							public FlowFunction<Local> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt, Unit retSite) {
								if (exitStmt instanceof ReturnStmt) {								
									ReturnStmt returnStmt = (ReturnStmt) exitStmt;
									Value op = returnStmt.getOp();
									if(op instanceof Local) {
										if(callSite instanceof DefinitionStmt) {
											DefinitionStmt defnStmt = (DefinitionStmt) callSite;
											Value leftOp = defnStmt.getLeftOp();
											if(leftOp instanceof Local) {
												final Local tgtLocal = (Local) leftOp;
												final Local retLocal = (Local) op;
												return new FlowFunction<Local>() {

													public Set<Local> computeTargets(Local source) {
														if(source==retLocal)
															return Collections.singleton(tgtLocal);
														return Collections.emptySet();
													}
													
												};
											}
										}
									}
								} 
								return KillAll.v();
							}

							public FlowFunction<Local> getCallToReturnFlowFunction(Unit call, Unit returnSite) {
								return Identity.v();
							}
						};						
					}

					public InterproceduralCFG<Unit,SootMethod> interproceduralCFG() {
						return new JimpleBasedInterproceduralCFG();
					}

					public Multimap<SootMethod, Local> initialSeeds() {
						return initialSeeds;
					}

					public Local zeroValue() {
						return zeroValue;
					}
				};
				
//				IFDSReachingDefinitions problem2 = new IFDSReachingDefinitions();
//				IFDSSolver solver2 = new IFDSSolver(problem2);
//				solver2.solve();
//				Unit ret = Scene.v().getMainMethod().getActiveBody().getUnits().getLast();
//				for(Object l: solver2.ifdsResultsAt(ret)) {
//					System.err.println(l);
//				}
				
				IFDSSolver<Unit,Local,SootMethod> solver = new IFDSSolver<Unit,Local,SootMethod>(problem);	
				solver.solve();
				Unit ret = Scene.v().getMainMethod().getActiveBody().getUnits().getLast();
				for(Local l: solver.ifdsResultsAt(ret)) {
					System.err.println(l);
				}
			}
		}));
		
		soot.Main.main(args);
	}

}
