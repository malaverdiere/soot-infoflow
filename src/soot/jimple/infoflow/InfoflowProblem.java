package soot.jimple.infoflow;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.PrimType;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Source;
import soot.jimple.infoflow.source.DefaultSourceManager;
import soot.jimple.infoflow.source.DumbSourceManager;
import soot.jimple.infoflow.source.SourceManager;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.interproc.ifds.FlowFunction;
import soot.jimple.interproc.ifds.FlowFunctions;
import soot.jimple.interproc.ifds.InterproceduralCFG;
import soot.jimple.interproc.ifds.flowfunc.Identity;
import soot.jimple.interproc.ifds.template.DefaultIFDSTabulationProblem;
import soot.jimple.interproc.ifds.template.JimpleBasedInterproceduralCFG;
import soot.toolkits.scalar.Pair;

public class InfoflowProblem extends DefaultIFDSTabulationProblem<Pair<Value, Value>, InterproceduralCFG<Unit, SootMethod>> {

	final Set<Unit> initialSeeds = new HashSet<Unit>();
	final List<Source> sourceList = new ArrayList<Source>();
	final Pair<Value, Value> zeroValue = new Pair<Value, Value>(new JimpleLocal("zero", NullType.v()), null);

	static boolean DEBUG = false;

	public FlowFunctions<Unit, Pair<Value, Value>, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Pair<Value, Value>, SootMethod>() {

			public FlowFunction<Pair<Value, Value>> getNormalFlowFunction(Unit src, Unit dest) {
				if (src instanceof Stmt && DEBUG) {

					System.out.println("Normal: " + ((Stmt) src));
				}
				if (src instanceof AssignStmt) {
					AssignStmt assignStmt = (AssignStmt) src;
					Value right = assignStmt.getRightOp();
					Value left = assignStmt.getLeftOp();

					// FIXME: easy solution: take whole array instead of position:
					if (left instanceof ArrayRef) {
						left = ((ArrayRef) left).getBase();
					}

					if (right instanceof Value && left instanceof Value) {
						final Value leftValue = left;
						final Value rightValue = right;

						return new FlowFunction<Pair<Value, Value>>() {

							public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
								boolean addLeftValue = false;

								// check if new infoflow is created here? Not necessary because this covers only calls of methods in the same class,
								// which should not be source methods (not part of android api)

								// normal check for infoflow
								if (rightValue instanceof JVirtualInvokeExpr || !source.equals(zeroValue)) {
									PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
									if (source.getO1() instanceof InstanceFieldRef && rightValue instanceof InstanceFieldRef) {
										InstanceFieldRef rightRef = (InstanceFieldRef) rightValue;
										InstanceFieldRef sourceRef = (InstanceFieldRef) source.getO1();

										if (rightRef.getField().getName().equals(sourceRef.getField().getName())) {
											Local rightBase = (Local) ((InstanceFieldRef) rightValue).getBase();
											PointsToSet ptsRight = pta.reachingObjects(rightBase);
											Local sourceBase = (Local) ((InstanceFieldRef) source.getO1()).getBase();
											PointsToSet ptsSource = pta.reachingObjects(sourceBase);
											if (ptsRight.hasNonEmptyIntersection(ptsSource)) {
												addLeftValue = true;
											}
										}
									}

									if (rightValue instanceof InstanceFieldRef && source.getO1() instanceof Local) {
										PointsToSet ptsSource = pta.reachingObjects((Local) source.getO1());
										InstanceFieldRef rightRef = (InstanceFieldRef) rightValue;
										PointsToSet ptsResult = pta.reachingObjects(ptsSource, rightRef.getField());

										if (!ptsResult.isEmpty()) {
											addLeftValue = true;
										}

									}

									if (rightValue instanceof StaticFieldRef && source.getO1() instanceof StaticFieldRef) {
										StaticFieldRef rightRef = (StaticFieldRef) rightValue;
										StaticFieldRef sourceRef = (StaticFieldRef) source.getO1();
										if (rightRef.getFieldRef().name().equals(sourceRef.getFieldRef().name()) && rightRef.getFieldRef().declaringClass().equals(sourceRef.getFieldRef().declaringClass())) {
											addLeftValue = true;
										}
									}

									if (rightValue instanceof ArrayRef) {
										Local rightBase = (Local) ((ArrayRef) rightValue).getBase();
										if (source.getO1().equals(rightBase) || (source.getO1() instanceof Local && pta.reachingObjects(rightBase).hasNonEmptyIntersection(pta.reachingObjects((Local) source.getO1())))) {
											addLeftValue = true;
										}
									}
									if (rightValue instanceof JCastExpr) {
										if (source.getO1().equals(((JCastExpr) rightValue).getOpBox().getValue())) {
											addLeftValue = true;
										}
									}
									// TODO: Change and see if is possible by just using the native call
									if (rightValue instanceof JVirtualInvokeExpr) {
										if (((JVirtualInvokeExpr) rightValue).getMethodRef().name().equals("clone") || ((JVirtualInvokeExpr) rightValue).getMethodRef().name().equals("concat")) { // TODO: sonderfall, welche noch?
											if (source.getO1().equals(((JVirtualInvokeExpr) rightValue).getBaseBox().getValue())) {
												addLeftValue = true;
											}
										}
									}

									// generic case, is true for Locals, ArrayRefs that are equal etc..
									if (source.getO1().equals(rightValue)) {
										addLeftValue = true;
									}
									// also check if there are two arrays (or anything else?) that point to the same..
									if (source.getO1() instanceof Local && rightValue instanceof Local) {
										PointsToSet ptsRight = pta.reachingObjects((Local) rightValue);
										PointsToSet ptsSource = pta.reachingObjects((Local) source.getO1());
										if (ptsRight.hasNonEmptyIntersection(ptsSource)) {
											addLeftValue = true;
										}
									}

									// if one of them is true -> add leftValue
									if (addLeftValue) {
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
				if (DEBUG) {
					System.out.println("Call " + ie);
				}
				final List<Value> callArgs = ie.getArgs();
				final List<Value> paramLocals = new ArrayList<Value>();
				for (int i = 0; i < dest.getParameterCount(); i++) {
					paramLocals.add(dest.getActiveBody().getParameterLocal(i));
				}
				return new FlowFunction<Pair<Value, Value>>() {

					public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
						Set<Pair<Value, Value>> res = new HashSet<Pair<Value, Value>>();
						int argIndex = callArgs.indexOf(source.getO1());
						if (argIndex > -1) {
							res.add(new Pair<Value, Value>(paramLocals.get(argIndex), source.getO2()));
						}
						if (source.getO1() instanceof FieldRef) {
							//FieldRef tempFieldRef = (FieldRef) source.getO1();
							
							//if (tempFieldRef.getField().getDeclaringClass().equals(dest.getDeclaringClass())) { // not enough because might be used in this class...
								
							res.add(source);
							//}
						}
						
						
						return res;
					}
				};
			}

			public FlowFunction<Pair<Value, Value>> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt, Unit retSite) {
				final SootMethod calleeMethod = callee;
				
				if (exitStmt instanceof Stmt & DEBUG) {
					System.out.println("ReturnExit: " + ((Stmt) exitStmt));
					System.out.println("ReturnStart: " + callSite.toString());
				}
				
				if (exitStmt instanceof ReturnStmt) {
					ReturnStmt returnStmt = (ReturnStmt) exitStmt;
					Value op = returnStmt.getOp();
					if (op instanceof Value) {
						if (callSite instanceof DefinitionStmt) {
							DefinitionStmt defnStmt = (DefinitionStmt) callSite;
							Value leftOp = defnStmt.getLeftOp();
							final Value tgtLocal = leftOp;
							final Value retLocal = op;
							return new FlowFunction<Pair<Value, Value>>() {

								public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
									if(source.getO2()!= null){
										//System.out.println("stop1");
									}
									Set<Pair<Value, Value>> res = new HashSet<Pair<Value, Value>>();
									if (source.getO1() instanceof FieldRef) {
										res.add(source);
									}
									if (source.getO1() == retLocal)
										res.add(new Pair<Value, Value>(tgtLocal, source.getO2()));
									return res;
								}

							};
						}

					}
				}

				return new FlowFunction<Pair<Value, Value>>() {

					public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
						if(source.getO2()!= null){
							//System.out.println("stop!"); //for ArrayListTest
						}
						
						Set<Pair<Value, Value>> res = new HashSet<Pair<Value, Value>>();
						if (source.getO1() instanceof Local) {
							PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
							PointsToSet ptsRight = pta.reachingObjects((Local) source.getO1());
							for (SootField globalField : calleeMethod.getDeclaringClass().getFields()) {
								if (globalField.isStatic()) {
									PointsToSet ptsGlobal = pta.reachingObjects(globalField);
									if (ptsRight.hasNonEmptyIntersection(ptsGlobal)) {
										res.add(new Pair<Value, Value>(Jimple.v().newStaticFieldRef(globalField.makeRef()), source.getO2()));
									}
								} else {
									//if (!pta.reachingObjects(ptsRight, globalField).isEmpty()) { //the following line does not work?
									PointsToSet ptsGlobal = pta.reachingObjects((Local)source.getO1(), globalField);
									if (!ptsGlobal.isEmpty()) {
										res.add(new Pair<Value, Value>(Jimple.v().newInstanceFieldRef(calleeMethod.getActiveBody().getThisLocal(), globalField.makeRef()),source.getO2()));
									}
								}
								
							}
						}
						if (source.getO1() instanceof FieldRef) {
							res.add(source);
						}
						return res;
					}

				};
			}

			public FlowFunction<Pair<Value, Value>> getCallToReturnFlowFunction(Unit call, Unit returnSite) {
				if (DEBUG) {
					System.out.println("C2R c: " + call);
					System.out.println("C2R r: " + returnSite);
				}
				SourceManager sourceManager;
				// TODO: for testing only:
				if (sourceList.size() == 0) {
					sourceManager = new DumbSourceManager();
				} else {
					sourceManager = new DefaultSourceManager(sourceList);
				}
				if (call instanceof InvokeStmt && ((InvokeStmt) call).getInvokeExpr().getMethod().isNative()) {
					InvokeStmt iStmt = (InvokeStmt) call;
					// Testoutput to collect all native calls from different runs of the analysis:
					try {
						FileWriter fstream = new FileWriter("nativeCalls.txt", true);
						BufferedWriter out = new BufferedWriter(fstream);
						out.write(iStmt.getInvokeExpr().getMethod().toString() + "\n");
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					final List<Value> callArgs = iStmt.getInvokeExpr().getArgs();
					final List<Value> paramLocals = new ArrayList<Value>();
					for (int i = 0; i < iStmt.getInvokeExpr().getArgCount(); i++) {
						Value argValue = iStmt.getInvokeExpr().getArg(i);
						if (!(argValue.getType() instanceof PrimType)) {
							paramLocals.add(iStmt.getInvokeExpr().getArg(i));
						}
					}
					return new FlowFunction<Pair<Value, Value>>() {

						public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {

							int argIndex = callArgs.indexOf(source.getO1());
							if (argIndex > -1) {
								// "res.add(new Pair<Value, Value>(paramLocals.get(argIndex), source.getO2()));" is not enough:
								// java uses call by value, but fields of complex objects can be changed (and tainted), so use this conservative approach:
								Set<Pair<Value, Value>> res = new HashSet<Pair<Value, Value>>();
								for (int i = 0; i < paramLocals.size(); i++) {
									res.add(new Pair<Value, Value>(paramLocals.get(i), source.getO2()));
								}
								return res;
							}
							return Collections.emptySet();
						}
					};
				}
				if (call instanceof JAssignStmt) {
					final JAssignStmt stmt = (JAssignStmt) call;

					if (sourceManager.isSourceMethod(stmt.getInvokeExpr().getMethod().getClass(), stmt.getInvokeExpr().getMethodRef().name())) {
						return new FlowFunction<Pair<Value, Value>>() {

							@Override
							public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
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

	public InfoflowProblem() {
		super(new JimpleBasedInterproceduralCFG());
	}

	public InfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
		super(icfg);
	}

	public Pair<Value, Value> createZeroValue() {
		if (zeroValue == null)
			return new Pair<Value, Value>(new JimpleLocal("zero", NullType.v()), null);
		return zeroValue;
	}

	@Override
	public Set<Unit> initialSeeds() {
		return initialSeeds;

	}
}
