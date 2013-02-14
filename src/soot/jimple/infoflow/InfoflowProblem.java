package soot.jimple.infoflow;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.solver.PathEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.EquivalentValue;
import soot.Local;
import soot.PrimType;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionWithPath;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.source.SourceSinkManager;
import soot.jimple.infoflow.util.BaseSelector;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

public class InfoflowProblem extends AbstractInfoflowProblem {

	private final static boolean DEBUG = true;

	final SourceSinkManager sourceSinkManager;

	@Override
	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			@Override
			public FlowFunction<Abstraction> getNormalFlowFunction(final Unit src, final Unit dest) {
				
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

					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							boolean addLeftValue = false;
							boolean keepAllFieldTaintStar = true;
							boolean forceFields = false;
							Set<Abstraction> res = new HashSet<Abstraction>();
							// shortcuts:
							// on NormalFlow taint cannot be created
							if (source.equals(zeroValue)) {
								return Collections.emptySet();
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
									Local sourceBase = source.getAccessPath().getPlainLocal();
									if (rightBase.equals(sourceBase)) {
										if (source.getAccessPath().isInstanceFieldRef()) {
											if (rightRef.getField().getName().equals(source.getAccessPath().getField())) {
												addLeftValue = true;
											}
										} else {
											addLeftValue = true;
											keepAllFieldTaintStar = false;
										}
									}
								}

								// indirect taint propagation:
								// if rightvalue is local and source is instancefield of this local:
								if (rightValue instanceof Local && source.getAccessPath().isInstanceFieldRef()) {
									Local base = source.getAccessPath().getPlainLocal();
									if (rightValue.equals(base)) {
										if (leftValue instanceof Local) {
											if (pathTracking == PathTrackingMethod.ForwardTracking)
												res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(leftValue), source.getSource(), source.getCorrespondingMethod(), ((AbstractionWithPath) source).getPropagationPath(), src));
											else
												res.add(new Abstraction(source.getAccessPath().copyWithNewValue(leftValue), source.getSource(), source.getCorrespondingMethod()));
										} else {
											// access path length = 1 - taint entire value if left is field reference
											if (pathTracking == PathTrackingMethod.ForwardTracking)
												res.add(new AbstractionWithPath(new EquivalentValue(leftValue), source.getSource(), source.getCorrespondingMethod(), ((AbstractionWithPath) source).getPropagationPath(), src, true));
											else
												res.add(new Abstraction(new EquivalentValue(leftValue), source.getSource(), source.getCorrespondingMethod(), true));
											forceFields = true;
										}
									}
								}

								if (rightValue instanceof ArrayRef) {
									Local rightBase = (Local) ((ArrayRef) rightValue).getBase();
									if (rightBase.equals(source.getAccessPath().getPlainValue())) {
										addLeftValue = true;
									}
								}

								// generic case, is true for Locals, ArrayRefs that are equal etc..
								if (rightValue.equals(source.getAccessPath().getPlainValue())) { // makes no sense to me: && !source.getAccessPath().isOnlyFieldsTainted()
									addLeftValue = true;
								}
							}
							// if one of them is true -> add leftValue
							if (addLeftValue) {
								res.add(source);

								if (pathTracking == PathTrackingMethod.ForwardTracking)
									res.add(new AbstractionWithPath(new EquivalentValue(leftValue), source.getSource(), interproceduralCFG().getMethodOf(src), ((AbstractionWithPath) source).getPropagationPath(), src, keepAllFieldTaintStar && source.getAccessPath().isOnlyFieldsTainted()));
								else
									res.add(new Abstraction(new EquivalentValue(leftValue), source.getSource(), interproceduralCFG().getMethodOf(src), keepAllFieldTaintStar && source.getAccessPath().isOnlyFieldsTainted()));

								if (!(leftValue.getType() instanceof PrimType) && !(leftValue instanceof Constant)) {
									// call backwards-check:
									Unit predUnit = getUnitBefore(src);
									Abstraction newAbs = new Abstraction(new EquivalentValue(leftValue), source.getSource(), interproceduralCFG().getMethodOf(src), (forceFields) ? true : keepAllFieldTaintStar && source.getAccessPath().isOnlyFieldsTainted());
									bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(newAbs, predUnit, newAbs));

								}
								return res;
							}
							return Collections.singleton(source);

						}
					};

				}
				return Identity.v();
			}

			@Override
			public FlowFunction<Abstraction> getCallFlowFunction(final Unit src, final SootMethod dest) {
				final Stmt stmt = (Stmt) src;
				final InvokeExpr ie = stmt.getInvokeExpr();
				final List<Value> callArgs = ie.getArgs();
				final List<Value> paramLocals = new ArrayList<Value>();
				for (int i = 0; i < dest.getParameterCount(); i++) {
					paramLocals.add(dest.getActiveBody().getParameterLocal(i));
				}

				return new FlowFunction<Abstraction>() {

					@Override
					public Set<Abstraction> computeTargets(Abstraction source) {
						if (taintWrapper != null && taintWrapper.supportsTaintWrappingForClass(ie.getMethod().getDeclaringClass())) {
							// taint is propagated in CallToReturnFunction, so we do not need any taint here:
							return Collections.emptySet();
						}
						if (source.equals(zeroValue)) {
							return Collections.singleton(source);
						}

						Value base = source.getAccessPath().getPlainValue();
						Set<Abstraction> res = new HashSet<Abstraction>();
						// if taintedobject is instancefieldRef we have to check if the object is delivered..
						if (source.getAccessPath().isInstanceFieldRef()) {

							// second, they might be changed as param - check this

							// first, instancefieldRefs must be propagated if they come from the same class:
							if (ie instanceof InstanceInvokeExpr) {
								InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;

								if (vie.getBase().equals(source.getAccessPath().getPlainLocal())) {
									if (pathTracking == PathTrackingMethod.ForwardTracking)
										res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(dest.getActiveBody().getThisLocal()), source.getSource(), dest, ((AbstractionWithPath) source).getPropagationPath(), stmt));
									else
										res.add(new Abstraction(source.getAccessPath().copyWithNewValue(dest.getActiveBody().getThisLocal()), source.getSource(), dest));
								}
							}
						}

						// check if whole object is tainted (happens with strings, for example:)
						if (!dest.isStatic() && ie instanceof InstanceInvokeExpr && source.getAccessPath().isLocal()) {
							InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
							// this might be enough because every call must happen with a local variable which is tainted itself:
							if (vie.getBase().equals(source.getAccessPath().getPlainValue())) {
								if (pathTracking == PathTrackingMethod.ForwardTracking)
									res.add(new AbstractionWithPath(new EquivalentValue(dest.getActiveBody().getThisLocal()), source.getSource(), dest, ((AbstractionWithPath) source).getPropagationPath(), stmt, source.getAccessPath().isOnlyFieldsTainted()));
								else
									res.add(new Abstraction(new EquivalentValue(dest.getActiveBody().getThisLocal()), source.getSource(), dest, source.getAccessPath().isOnlyFieldsTainted()));
							}
						}

						// check if param is tainted:
						for (int i = 0; i < callArgs.size(); i++) {
							if (callArgs.get(i).equals(base)) {
								if (pathTracking == PathTrackingMethod.ForwardTracking)
									res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(paramLocals.get(i)), source.getSource(), dest, ((AbstractionWithPath) source).getPropagationPath(), stmt));
								else
									res.add(new Abstraction(source.getAccessPath().copyWithNewValue(paramLocals.get(i)), source.getSource(), dest));
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

			@Override
			public FlowFunction<Abstraction> getReturnFlowFunction(final Unit callSite, final SootMethod callee, final Unit exitStmt, final Unit retSite) {
				return new FlowFunction<Abstraction>() {

					@Override
					public Set<Abstraction> computeTargets(Abstraction source) {
						if (source.equals(zeroValue)) {
							return Collections.emptySet();
						}
						Set<Abstraction> res = new HashSet<Abstraction>();
						// if we have a returnStmt we have to look at the returned value:
						if (exitStmt instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) exitStmt;
							Value retLocal = returnStmt.getOp();

							if (callSite instanceof DefinitionStmt) {
								DefinitionStmt defnStmt = (DefinitionStmt) callSite;
								Value leftOp = defnStmt.getLeftOp();
								if (retLocal.equals(source.getAccessPath().getPlainValue())) {
									if (pathTracking == PathTrackingMethod.ForwardTracking)
										res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(leftOp), source.getSource(), interproceduralCFG().getMethodOf(callSite), ((AbstractionWithPath) source).getPropagationPath(), exitStmt));
									else
										res.add(new Abstraction(source.getAccessPath().copyWithNewValue(leftOp), source.getSource(), interproceduralCFG().getMethodOf(callSite)));
								}
								
								// TODO: think about it - is this necessary?
								// if(forwardbackward){
								// //call backwards-check:
								// Unit predUnit = getUnitBefore(callUnit);
								// Abstraction newAbs = new Abstraction(new EquivalentValue(leftValue), source.getSource(), calleeMethod,
								// source.getAccessPath().isOnlyFieldsTainted());
								// bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(newAbs, predUnit, newAbs));
								// }

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
						for (int i = 0; i < callee.getParameterCount(); i++) {
							if (callee.getActiveBody().getParameterLocal(i).equals(sourceBase)) { // or pts?
								if (callSite instanceof Stmt) {
									Stmt iStmt = (Stmt) callSite;
									originalCallArg = iStmt.getInvokeExpr().getArg(i);
									if (!(originalCallArg instanceof Constant) && !(originalCallArg.getType() instanceof PrimType)) {
										Abstraction abs;
										if (pathTracking == PathTrackingMethod.ForwardTracking)
											abs = new AbstractionWithPath(source.getAccessPath().copyWithNewValue(originalCallArg), source.getSource(), interproceduralCFG().getMethodOf(callSite), ((AbstractionWithPath) source).getPropagationPath(), exitStmt);
										else
											abs = new Abstraction(source.getAccessPath().copyWithNewValue(originalCallArg), source.getSource(), interproceduralCFG().getMethodOf(callSite));
										res.add(abs);

										// call backwards-check:
										Unit predUnit = getUnitBefore(callSite);
										bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(abs, predUnit, abs));

									}
								}
							}
						}

						Local thisL = null;
						if (!callee.isStatic()) {
							thisL = callee.getActiveBody().getThisLocal();
						}
						if (thisL != null) {
							if (thisL.equals(sourceBase)) {

								if (thisL.equals(sourceBase)) {
									boolean param = false;
									// check if it is not one of the params (then we have already fixed it)
									for (int i = 0; i < callee.getParameterCount(); i++) {
										if (callee.getActiveBody().getParameterLocal(i).equals(sourceBase)) {
											param = true;
										}
									}
									if (!param) {
										if (callSite instanceof Stmt) {
											Stmt stmt = (Stmt) callSite;
											if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
												InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
												if (pathTracking == PathTrackingMethod.ForwardTracking)
													res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(iIExpr.getBase()), source.getSource(), interproceduralCFG().getMethodOf(callSite), ((AbstractionWithPath) source).getPropagationPath(), exitStmt));
												else
													res.add(new Abstraction(source.getAccessPath().copyWithNewValue(iIExpr.getBase()), source.getSource(), interproceduralCFG().getMethodOf(callSite)));
											}
										}
									}
								}
							}
						}

						return res;
					}

				};
			}

			@Override
			public FlowFunction<Abstraction> getCallToReturnFlowFunction(final Unit call, Unit returnSite) {
				final Unit unit = returnSite;
				// special treatment for native methods:
				if (call instanceof Stmt) {
					final Stmt iStmt = (Stmt) call;
					final List<Value> callArgs = iStmt.getInvokeExpr().getArgs();

					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							Set<Abstraction> res = new HashSet<Abstraction>();
							res.add(source);

							if (taintWrapper != null && taintWrapper.supportsTaintWrappingForClass(iStmt.getInvokeExpr().getMethod().getDeclaringClass())) {
								int taintedPos = -1;
								for (int i = 0; i < callArgs.size(); i++) {
									if (source.getAccessPath().isLocal() && callArgs.get(i).equals(source.getAccessPath().getPlainValue())) {
										taintedPos = i;
										break;
									}
								}
								Value taintedBase = null;
								if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
									InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
									if (iiExpr.getBase().equals(source.getAccessPath().getPlainValue())) {
										if (source.getAccessPath().isLocal()) {
											taintedBase = iiExpr.getBase();
										} else if (source.getAccessPath().isInstanceFieldRef()) {
											taintedBase = new JInstanceFieldRef(iiExpr.getBase(), iStmt.getInvokeExpr().getMethod().getDeclaringClass().getFieldByName(source.getAccessPath().getField()).makeRef());
										}
									}
									if (source.getAccessPath().isStaticFieldRef()) {
										// TODO
									}
								}

								List<Value> vals = taintWrapper.getTaintsForMethod(iStmt, taintedPos, taintedBase);
								if (vals != null)
									for (Value val : vals)
										if (pathTracking == PathTrackingMethod.ForwardTracking)
											res.add(new AbstractionWithPath(new EquivalentValue(val), source.getSource(), source.getCorrespondingMethod(), ((AbstractionWithPath) source).getPropagationPath(), iStmt, false));
										else
											res.add(new Abstraction(new EquivalentValue(val), source.getSource(), source.getCorrespondingMethod(), false));
							}

							if (iStmt.getInvokeExpr().getMethod().isNative()) {
								if (callArgs.contains(source.getAccessPath().getPlainValue())) {
									// java uses call by value, but fields of complex objects can be changed (and tainted), so use this conservative approach:
									Set<Abstraction> taints = ncHandler.getTaintedValues(iStmt, source, callArgs, interproceduralCFG().getMethodOf(unit));
									res.addAll(taints);
									for (Abstraction a : taints) {
										// call backwards-check:
										Unit predUnit = getUnitBefore(call);
										bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(a, predUnit, a));

									}

								}
							}

							if (iStmt instanceof JAssignStmt) {
								final JAssignStmt stmt = (JAssignStmt) iStmt;

								if (sourceSinkManager.isSourceMethod(stmt.getInvokeExpr().getMethod())) {
									if (DEBUG)
										System.out.println("Found source: " + stmt.getInvokeExpr().getMethod());
									if (pathTracking == PathTrackingMethod.ForwardTracking)
										res.add(new AbstractionWithPath(new EquivalentValue(stmt.getLeftOp()), new EquivalentValue(stmt.getInvokeExpr()), interproceduralCFG().getMethodOf(unit), ((AbstractionWithPath) source).getPropagationPath(), call, false));
									else
										res.add(new Abstraction(new EquivalentValue(stmt.getLeftOp()), new EquivalentValue(stmt.getInvokeExpr()), interproceduralCFG().getMethodOf(unit), false));
									res.remove(zeroValue);
								}
							}

							// if we have called a sink we have to store the path from the source - in case one of the params is tainted!
							if (sourceSinkManager.isSinkMethod(iStmt.getInvokeExpr().getMethod())) {
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
									if (pathTracking != PathTrackingMethod.NoTracking)
										results.addResult(iStmt.getInvokeExpr().getMethod().toString(), source.getSource().getValue().toString(), ((AbstractionWithPath) source).getPropagationPathAsString(interproceduralCFG()), interproceduralCFG().getMethodOf(call) + ": " + call.toString());
									else
										results.addResult(iStmt.getInvokeExpr().getMethod().toString(), source.getSource().getValue().toString());
								}
								// if the base object which executes the method is tainted the sink is reached, too.
								if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
									InstanceInvokeExpr vie = (InstanceInvokeExpr) iStmt.getInvokeExpr();
									if (vie.getBase().equals(source.getAccessPath().getPlainValue())) {
										if (pathTracking != PathTrackingMethod.NoTracking)
											results.addResult(iStmt.getInvokeExpr().getMethod().toString(), source.getSource().getValue().toString(), ((AbstractionWithPath) source).getPropagationPathAsString(interproceduralCFG()), interproceduralCFG().getMethodOf(call) + ": " + call.toString());
										else
											results.addResult(iStmt.getInvokeExpr().getMethod().toString(), source.getSource().getValue().toString());
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

	public InfoflowProblem(List<String> sourceList, List<String> sinkList) {
		super(new JimpleBasedInterproceduralCFG());
		this.sourceSinkManager = new DefaultSourceSinkManager(sourceList, sinkList);
	}

	public InfoflowProblem(SourceSinkManager sourceSinkManager) {
		super(new JimpleBasedInterproceduralCFG());
		this.sourceSinkManager = sourceSinkManager;
	}

	public InfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg, List<String> sourceList, List<String> sinkList) {
		super(icfg);
		this.sourceSinkManager = new DefaultSourceSinkManager(sourceList, sinkList);
	}

	public InfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg, SourceSinkManager sourceSinkManager) {
		super(icfg);
		this.sourceSinkManager = sourceSinkManager;
	}

	/**
	 * returns the unit before the given unit (or the unit itself if it is the first unit)
	 * 
	 * @param u
	 * @return
	 */
	private Unit getUnitBefore(Unit u) {
		SootMethod m = interproceduralCFG().getMethodOf(u);
		Unit preUnit = u;
		for (Unit currentUnit : m.getActiveBody().getUnits()) {
			if (currentUnit.equals(u)) {
				return preUnit;
			}
			preUnit = currentUnit;
		}
		return u;

	}

}