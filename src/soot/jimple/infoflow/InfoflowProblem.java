package soot.jimple.infoflow;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.EquivalentValue;
import soot.Local;
import soot.NullType;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.PrimType;
import soot.Scene;
import soot.SootField;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.nativ.DefaultNativeCallHandler;
import soot.jimple.infoflow.nativ.NativeCallHandler;
import soot.jimple.infoflow.source.DefaultSourceManager;
import soot.jimple.infoflow.source.SourceManager;
import soot.jimple.infoflow.util.BaseSelector;
import soot.jimple.internal.InvokeExprBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

public class InfoflowProblem extends AbstractInfoflowProblem {

	final SourceManager sourceManager;
	final List<String> sinks;
	final Abstraction zeroValue = new Abstraction(new EquivalentValue(new JimpleLocal("zero", NullType.v())), null, null);

	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			public FlowFunction<Abstraction> getNormalFlowFunction(final Unit src, Unit dest) {
				// taint is propagated with assignStmt
				if (src instanceof AssignStmt) {
					AssignStmt assignStmt = (AssignStmt) src;
					Value right = assignStmt.getRightOp();
					Value left = assignStmt.getLeftOp();

					// find rightValue (remove casts):
					right = BaseSelector.selectBase(right, true);

					// find appropriate leftValue:
					left = BaseSelector.selectBase(left, false);

					final Value leftValue = left;
					final Value rightValue = right;
					final Unit srcUnit = src;

					return new FlowFunction<Abstraction>() {

						public Set<Abstraction> computeTargets(Abstraction source) {
							boolean addLeftValue = false;
							Set<Abstraction> res = new HashSet<Abstraction>();
							PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
							// shortcuts:
							// on NormalFlow taint cannot be created:
							if (source.equals(zeroValue)) {
								return Collections.singleton(source);
							}
							// check if static variable is tainted (same name, same class)
							if (source.getAccessPath().isStaticFieldRef()) {
								if (rightValue instanceof StaticFieldRef) {
									StaticFieldRef rightRef = (StaticFieldRef) rightValue;
									if (source.getAccessPath().getField().equals(InfoflowProblem.getStaticFieldRefStringRepresentation(rightRef))) {
										addLeftValue = true;
									}
								}
							} else {
								// if both are fields, we have to compare their fieldName via equals and their bases via PTS
								// might happen that source is local because of max(length(accesspath)) == 1
								if (rightValue instanceof InstanceFieldRef) {
									InstanceFieldRef rightRef = (InstanceFieldRef) rightValue;
									Local rightBase = (Local) rightRef.getBase();
									PointsToSet ptsRight = pta.reachingObjects(rightBase);
									Local sourceBase = (Local) source.getAccessPath().getPlainValue();
									PointsToSet ptsSource = pta.reachingObjects(sourceBase);
									if (ptsRight.hasNonEmptyIntersection(ptsSource)) {
										if (source.getAccessPath().isInstanceFieldRef()) {
											if (rightRef.getField().getName().equals(source.getAccessPath().getField())) {
												addLeftValue = true;
											}
										} else {
											addLeftValue = true;
										}
									}
								}

								// indirect taint propagation:
								// if rightvalue is local and source is instancefield of this local:
								if (rightValue instanceof Local && source.getAccessPath().isInstanceFieldRef()) {
									Local base = (Local) source.getAccessPath().getPlainValue(); // ?
									PointsToSet ptsSourceBase = pta.reachingObjects(base);
									PointsToSet ptsRight = pta.reachingObjects((Local) rightValue);
									if (ptsSourceBase.hasNonEmptyIntersection(ptsRight)) {
										if (leftValue instanceof Local) {
											res.add(new Abstraction(source.getAccessPath().copyWithNewValue(leftValue), source.getSource(), source.getCorrespondingMethod()));
										} else {
											// access path length = 1 - taint entire value if left is field reference
											res.add(new Abstraction(new EquivalentValue(leftValue), source.getSource(), source.getCorrespondingMethod()));
										}
									}
								}

								if (rightValue instanceof ArrayRef) {
									Local rightBase = (Local) ((ArrayRef) rightValue).getBase();
									if (rightBase.equals(source.getAccessPath().getPlainValue()) || (source.getAccessPath().isLocal() && pta.reachingObjects(rightBase).hasNonEmptyIntersection(pta.reachingObjects((Local) source.getAccessPath().getPlainValue())))) {
										addLeftValue = true;
									}
								}

								// generic case, is true for Locals, ArrayRefs that are equal etc..
								if (rightValue.equals(source.getAccessPath().getPlainValue())) {
									addLeftValue = true;
								}
							}
							// if one of them is true -> add leftValue
							if (addLeftValue) {

								res.add(source);
								res.add(new Abstraction(new EquivalentValue(leftValue), source.getSource(), interproceduralCFG().getMethodOf(srcUnit)));

								SootMethod m = interproceduralCFG().getMethodOf(srcUnit);
								if (leftValue instanceof InstanceFieldRef) {
									InstanceFieldRef ifr = (InstanceFieldRef) leftValue;

									Set<Value> aliases = getAliasesinMethod(m.getActiveBody().getUnits(), src, ifr.getBase(), ifr.getFieldRef());
									for (Value v : aliases) {
										res.add(new Abstraction(new EquivalentValue(v), source.getSource(), interproceduralCFG().getMethodOf(srcUnit)));
									}
								}
								return res;
							}
							return Collections.singleton(source);

						}
					};

				}
				return Identity.v();
			}

			public FlowFunction<Abstraction> getCallFlowFunction(Unit src, final SootMethod dest) {
				final Stmt stmt = (Stmt) src;
				final InvokeExpr ie = stmt.getInvokeExpr();
				final List<Value> callArgs = ie.getArgs();
				final List<Value> paramLocals = new ArrayList<Value>();
				for (int i = 0; i < dest.getParameterCount(); i++) {
					paramLocals.add(dest.getActiveBody().getParameterLocal(i));
				}
				return new FlowFunction<Abstraction>() {

					public Set<Abstraction> computeTargets(Abstraction source) {
						if (source.equals(zeroValue)) {
							return Collections.singleton(source);
						}

						PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
						Value base = source.getAccessPath().getPlainValue();
						Set<Abstraction> res = new HashSet<Abstraction>();
						// if taintedobject is instancefieldRef we have to check if the object is delivered..
						if (source.getAccessPath().isInstanceFieldRef()) {

							// second, they might be changed as param - check this

							// first, instancefieldRefs must be propagated if they come from the same class:
							if (ie instanceof InstanceInvokeExpr) {
								InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;

								PointsToSet ptsSource = pta.reachingObjects((Local) base);
								PointsToSet ptsCall = pta.reachingObjects((Local) vie.getBase());
								if (ptsCall.hasNonEmptyIntersection(ptsSource)) {
									res.add(new Abstraction(source.getAccessPath().copyWithNewValue(dest.getActiveBody().getThisLocal()), source.getSource(), dest));
								}
							}

						}

						// check if whole object is tainted (happens with strings, for example:)
						if (!dest.isStatic() && ie instanceof InstanceInvokeExpr && source.getAccessPath().isLocal()) {
							InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
							// this might be enough because every call must happen with a local variable which is tainted itself:
							if (vie.getBase().equals(source.getAccessPath().getPlainValue())) {
								res.add(new Abstraction(new EquivalentValue(dest.getActiveBody().getThisLocal()), source.getSource(), dest));
							}
						}

						// check if param is tainted:
						for (int i = 0; i < callArgs.size(); i++) {
							if (callArgs.get(i).equals(base)) {
								Abstraction abs = new Abstraction(source.getAccessPath().copyWithNewValue(paramLocals.get(i)), source.getSource(), dest);
								res.add(abs);
							}
						}

						// staticfieldRefs must be analyzed even if they are not part of the params:
						if (source.getAccessPath().isStaticFieldRef()) {
							res.add(source);
						}

						return res;
					}
				};
			}

			public FlowFunction<Abstraction> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt, final Unit retSite) {
				final SootMethod calleeMethod = callee;
				final Unit callUnit = callSite;
				final Unit exitUnit = exitStmt;

				return new FlowFunction<Abstraction>() {

					public Set<Abstraction> computeTargets(Abstraction source) {
						if (source.equals(zeroValue)) {
							return Collections.singleton(source);
						}

						Set<Abstraction> res = new HashSet<Abstraction>();

						// if we have a returnStmt we have to look at the returned value:
						if (exitUnit instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) exitUnit;
							Value retLocal = returnStmt.getOp();

							if (callUnit instanceof DefinitionStmt) {
								DefinitionStmt defnStmt = (DefinitionStmt) callUnit;
								Value leftOp = defnStmt.getLeftOp();
								if (retLocal.equals(source.getAccessPath().getPlainValue())) {
									res.add(new Abstraction(source.getAccessPath().copyWithNewValue(leftOp), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
								}
								// this is required for sublists, because they assign the list to the return variable and call a method that taints the list afterwards
								Set<Value> aliases = getAliasesinMethod(calleeMethod.getActiveBody().getUnits(), retSite, retLocal, null);
								for (Value v : aliases) {
									if (v.equals(source.getAccessPath().getPlainValue())) {
										res.add(new Abstraction(source.getAccessPath().copyWithNewValue(leftOp), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
									}
								}
							}
						}

						// easy: static
						if (source.getAccessPath().isStaticFieldRef()) {
							res.add(source);
						}

						// checks: this/params/fields

						// check one of the call params are tainted (not if simple type)
						Value sourceBase = source.getAccessPath().getPlainValue();
						Value originalCallArg = null;
						for (int i = 0; i < calleeMethod.getParameterCount(); i++) {
							if (calleeMethod.getActiveBody().getParameterLocal(i).equals(sourceBase)) { // or pts?
								if (callUnit instanceof Stmt) {
									Stmt iStmt = (Stmt) callUnit;
									originalCallArg = iStmt.getInvokeExpr().getArg(i);
									if (!(originalCallArg instanceof NullConstant) && !(originalCallArg.getType() instanceof PrimType)) {
										res.add(new Abstraction(source.getAccessPath().copyWithNewValue(originalCallArg), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
									}
								}
							}
						}

						Local thisL = null;
						PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
						PointsToSet ptsSource = pta.reachingObjects((Local) sourceBase);
						if (!calleeMethod.isStatic()) {
							thisL = calleeMethod.getActiveBody().getThisLocal();
						}
						if (thisL != null) {
							if (thisL.equals(sourceBase)) {
								// TODO: either remove PTS check here or remove the if-condition above!
								// there is only one case in which this must be added, too: if the caller-Method has the same thisLocal - check this:
								// for super-calls we have to use pts
								PointsToSet ptsThis = pta.reachingObjects(thisL);

								if (ptsSource.hasNonEmptyIntersection(ptsThis) || sourceBase.equals(thisL)) {
									boolean param = false;
									// check if it is not one of the params (then we have already fixed it)
									for (int i = 0; i < calleeMethod.getParameterCount(); i++) {
										if (calleeMethod.getActiveBody().getParameterLocal(i).equals(sourceBase)) {
											param = true;
										}
									}
									if (!param) {
										if (callUnit instanceof JInvokeStmt) {
											JInvokeStmt jiStmt = (JInvokeStmt) callUnit;
											if (jiStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
												InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) jiStmt.getInvokeExpr();
												res.add(new Abstraction(source.getAccessPath().copyWithNewValue(iIExpr.getBase()), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
											}
										}
										if (callUnit instanceof JAssignStmt) {
											JAssignStmt jaStmt = (JAssignStmt) callUnit;
											if (jaStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
												InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) jaStmt.getInvokeExpr();
												res.add(new Abstraction(source.getAccessPath().copyWithNewValue(iIExpr.getBase()), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
											}
										}
									}
								}
							}
							// remember that we only support max(length(accesspath))==1 -> if source is a fieldref, only its base is taken!
							for (SootField globalField : calleeMethod.getDeclaringClass().getFields()) {
								if (!globalField.isStatic()) { // else is checked later
									PointsToSet ptsGlobal = pta.reachingObjects(calleeMethod.getActiveBody().getThisLocal(), globalField);
									if (ptsGlobal.hasNonEmptyIntersection(ptsSource)) {
										Local callBaseVar = null;
										if (callUnit instanceof JAssignStmt) {
											callBaseVar = (Local) ((InstanceInvokeExpr) ((JAssignStmt) callUnit).getInvokeExpr()).getBase();
										}

										if (callUnit instanceof JInvokeStmt) {
											JInvokeStmt iStmt = (JInvokeStmt) callUnit;
											InvokeExprBox ieb = (InvokeExprBox) iStmt.getInvokeExprBox();
											Value v = ieb.getValue();
											InstanceInvokeExpr jvie = (InstanceInvokeExpr) v;
											callBaseVar = (Local) jvie.getBase();
										}
										if (callBaseVar != null) {
											SootFieldRef ref = globalField.makeRef();
											InstanceFieldRef fRef = Jimple.v().newInstanceFieldRef(callBaseVar, ref);
											res.add(new Abstraction(new EquivalentValue(fRef), source.getSource(), calleeMethod));
										}
									}
								}
							}
						}

						for (SootField globalField : calleeMethod.getDeclaringClass().getFields()) {
							if (globalField.isStatic()) {
								PointsToSet ptsGlobal = pta.reachingObjects(globalField);
								if (ptsSource.hasNonEmptyIntersection(ptsGlobal)) {
									res.add(new Abstraction(new EquivalentValue(Jimple.v().newStaticFieldRef(globalField.makeRef())), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
								}
							}
						}

						return res;
					}

				};
			}

			public FlowFunction<Abstraction> getCallToReturnFlowFunction(Unit call, Unit returnSite) {
				final Unit unit = returnSite;
				// special treatment for native methods:
				if (call instanceof Stmt) {
					final Stmt iStmt = (Stmt) call;
					final List<Value> callArgs = iStmt.getInvokeExpr().getArgs();

					// Testoutput to collect all native calls from different runs of the analysis:
					if (iStmt.getInvokeExpr().getMethod().isNative()) {
						try {
							FileWriter fstream = new FileWriter("nativeCalls.txt", true);
							BufferedWriter out = new BufferedWriter(fstream);
							out.write(iStmt.getInvokeExpr().getMethod().toString() + "\n");
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return new FlowFunction<Abstraction>() {

						public Set<Abstraction> computeTargets(Abstraction source) {
							Set<Abstraction> res = new HashSet<Abstraction>();
							res.add(source);

							if (iStmt.getInvokeExpr().getMethod().isNative()) {
								if (callArgs.contains(source.getAccessPath().getPlainValue())) {
									// java uses call by value, but fields of complex objects can be changed (and tainted), so use this conservative approach:

									NativeCallHandler ncHandler = new DefaultNativeCallHandler();
									res.addAll(ncHandler.getTaintedValues(iStmt, source, callArgs, interproceduralCFG().getMethodOf(unit)));
								}
							}

							if (iStmt instanceof JAssignStmt) {
								final JAssignStmt stmt = (JAssignStmt) iStmt;

								if (sourceManager.isSourceMethod(stmt.getInvokeExpr().getMethod())) {
									res.add(new Abstraction(new EquivalentValue(stmt.getLeftOp()), new EquivalentValue(stmt.getInvokeExpr()), interproceduralCFG().getMethodOf(unit)));
								}
							}

							// if we have called a sink we have to store the path from the source - in case one of the params is tainted!
							if (sinks.contains(iStmt.getInvokeExpr().getMethod().toString())) {
								boolean taintedParam = false;
								for (int i = 0; i < callArgs.size(); i++) {
									if (callArgs.get(i).equals(source.getAccessPath().getPlainValue())) {
										taintedParam = true;
										break;
									}
									if (source.getAccessPath().isStaticFieldRef()) {
										if (source.getAccessPath().getField().substring(0, source.getAccessPath().getField().lastIndexOf('.')).equals((callArgs.get(i)).getType().toString())) {
											taintedParam = true;
											break;
										}
									}
								}

								if (taintedParam) {
									if (!results.containsKey(iStmt.getInvokeExpr().getMethod().toString())) {
										List<String> list = new ArrayList<String>();
										list.add(source.getSource().getValue().toString());
										results.put(iStmt.getInvokeExpr().getMethod().toString(), list);
									} else {
										results.get(iStmt.getInvokeExpr().getMethod().toString()).add(source.getSource().getValue().toString());
									}
								}
								// TODO: required? only for LocalAnalysis at the moment: if the base object which executes the method is tainted the sink is reached, too.
								if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
									InstanceInvokeExpr vie = (InstanceInvokeExpr) iStmt.getInvokeExpr();
									if (vie.getBase().equals(source.getAccessPath().getPlainValue())) {
										if (!results.containsKey(iStmt.getInvokeExpr().getMethod().toString())) {
											List<String> list = new ArrayList<String>();
											list.add(source.getSource().getValue().toString());
											results.put(iStmt.getInvokeExpr().getMethod().toString(), list);
										} else {
											results.get(iStmt.getInvokeExpr().getMethod().toString()).add(source.getSource().getValue().toString());
										}
									}
								}
							}
							return res;
						}
					};
				}
				return Identity.v();
			}
		};
	}

	public InfoflowProblem(List<String> sourceList, List<String> sinks) {
		super(new JimpleBasedInterproceduralCFG());
		sourceManager = new DefaultSourceManager(sourceList);
		this.sinks = sinks;
	}

	public InfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg, List<String> sourceList, List<String> sinks) {
		super(icfg);
		sourceManager = new DefaultSourceManager(sourceList);
		this.sinks = sinks;
	}

	public Abstraction createZeroValue() {
		if (zeroValue == null)
			return new Abstraction(new EquivalentValue(new JimpleLocal("zero", NullType.v())), null, null);

		return zeroValue;
	}

	@Override
	public Set<Unit> initialSeeds() {
		return initialSeeds;
	}
}