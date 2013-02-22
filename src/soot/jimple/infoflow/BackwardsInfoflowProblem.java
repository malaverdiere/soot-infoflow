package soot.jimple.infoflow;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;
import heros.solver.PathEdge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import soot.EquivalentValue;
import soot.Local;
import soot.PrimType;
import soot.RefType;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionWithPath;
import soot.jimple.infoflow.heros.InfoflowSolver;
import soot.jimple.infoflow.nativ.DefaultNativeCallHandler;
import soot.jimple.infoflow.nativ.NativeCallHandler;
import soot.jimple.infoflow.util.BaseSelector;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.toolkits.ide.icfg.BackwardsInterproceduralCFG;

public class BackwardsInfoflowProblem extends AbstractInfoflowProblem {
	InfoflowSolver fSolver;

	public void setTaintWrapper(ITaintPropagationWrapper wrapper) {
		taintWrapper = wrapper;
	}

	public BackwardsInfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
		super(icfg);
	}

	public BackwardsInfoflowProblem() {
		super(new BackwardsInterproceduralCFG());
	}

	public void setForwardSolver(InfoflowSolver forwardSolver) {
		fSolver = forwardSolver;
	}

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
					right = BaseSelector.selectBase(right, false);

					// find appropriate leftValue:
					left = BaseSelector.selectBase(left, true);

					final Value leftValue = left;
					final Value rightValue = right;

					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							
							boolean addRightValue = false;
							boolean keepAllFieldTaintStar = true;
							Set<Abstraction> res = new HashSet<Abstraction>();
							// shortcuts:
							// on NormalFlow taint cannot be created:
							if (source.equals(zeroValue)) {
								return Collections.emptySet();
							}
							// if we have the tainted value on the right side of the assignment, we have to start a new forward task:
							if (rightValue instanceof InstanceFieldRef) {
								InstanceFieldRef ref = (InstanceFieldRef) rightValue;

								if (triggerReverseFlow(rightValue, source) && ref.getBase().equals(source.getAccessPath().getPlainValue()) && ref.getField().getName().equals(source.getAccessPath().getField())) { //not required: DataTypeHandler.isPrimTypeOrString(ref.getField()) &&
									Abstraction abs = source.deriveNewAbstraction(new EquivalentValue(leftValue), keepAllFieldTaintStar && source.getAccessPath().isOnlyFieldsTainted());
									// this should be successor (but successor is reversed because backwardsproblem, so predecessor is required.. -> but this should work, too:
									fSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(abs, src, abs));

								}
							}else{
								if (rightValue.equals(source.getAccessPath().getPlainValue()) && triggerReverseFlow(rightValue, source)) {
									Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(leftValue));
									fSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(abs, src, abs));
								}
							}
								

							
							// termination shortcut:
							if (leftValue.equals(source.getAccessPath().getPlainValue()) && rightValue instanceof NewExpr) {
								return Collections.emptySet();
							}

							// if we have the tainted value on the left side of the assignment, we have to track the right side of the assignment

							// we do not track StaticFieldRefs during BackwardAnalysis:
							if (!(leftValue instanceof StaticFieldRef)) {
								// if both are fields, we have to compare their fieldName via equals and their bases via PTS
								// might happen that source is local because of max(length(accesspath)) == 1
								if (leftValue instanceof InstanceFieldRef) {
									InstanceFieldRef leftRef = (InstanceFieldRef) leftValue;
									Local leftBase = (Local) leftRef.getBase();
									Local sourceBase = source.getAccessPath().getPlainLocal();
									if (leftBase.equals(sourceBase)) {
										if (source.getAccessPath().isInstanceFieldRef()) {
											if (leftRef.getField().getName().equals(source.getAccessPath().getField())) {
												addRightValue = true;
											}
										} else {
											addRightValue = true;
											keepAllFieldTaintStar = false;
										}
									}
									// indirect taint propagation:
									// if leftValue is local and source is instancefield of this local:
								} else if (leftValue instanceof Local && source.getAccessPath().isInstanceFieldRef()) {
									Local base = source.getAccessPath().getPlainLocal(); // ?
									if (leftValue.equals(base)) {
										if (rightValue instanceof Local) {
											if (pathTracking == PathTrackingMethod.ForwardTracking)
												res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(rightValue), source.getSource(), ((AbstractionWithPath) source).getPropagationPath()));
											else
												res.add(source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(rightValue)));
										} else {
											// access path length = 1 - taint entire value if left is field reference
											if (pathTracking == PathTrackingMethod.ForwardTracking)
												res.add(new AbstractionWithPath(new EquivalentValue(rightValue), source.getSource(), true));
											else
												res.add(source.deriveNewAbstraction(new EquivalentValue(rightValue), true));
										}
									}
								} else if (leftValue instanceof ArrayRef) {
									Local leftBase = (Local) ((ArrayRef) leftValue).getBase();
									if (leftBase.equals(source.getAccessPath().getPlainValue())) {
										addRightValue = true;
									}
									// generic case, is true for Locals, ArrayRefs that are equal etc..
								} else if (leftValue.equals(source.getAccessPath().getPlainValue())) {
									addRightValue = true;
								}
							}
							// if one of them is true -> add rightValue
							if (addRightValue) {
								if(rightValue.getType() instanceof PrimType || (rightValue.getType() instanceof RefType && ((RefType)rightValue.getType()).getClassName().equals("java.lang.String"))){
									return Collections.emptySet();
								}
								
								if (pathTracking == PathTrackingMethod.ForwardTracking)
									res.add(new AbstractionWithPath(new EquivalentValue(rightValue), source.getSource(), keepAllFieldTaintStar && source.getAccessPath().isOnlyFieldsTainted()));
								else
									res.add(source.deriveNewAbstraction(new EquivalentValue(rightValue), keepAllFieldTaintStar && source.getAccessPath().isOnlyFieldsTainted()));
							}
							if (!res.isEmpty()) {
								// we have to send forward pass, for example for
								// $r1 = l0.<java.lang.AbstractStringBuilder: char[] value>
								Abstraction a = res.iterator().next();
								if (triggerReverseFlow(a.getAccessPath().getPlainValue(), a)) {
									fSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(a, src, a));
								}
								return res;
							} else {
								return Collections.singleton(source); 
							}

						}
					};

				}
				return Identity.v();
			}

			@Override
			public FlowFunction<Abstraction> getCallFlowFunction(final Unit src, final SootMethod dest) {
				final SootMethod method = dest;

				return new FlowFunction<Abstraction>() {

					@Override
					public Set<Abstraction> computeTargets(Abstraction source) {
						
						if (source.equals(zeroValue)) {
							return Collections.emptySet();
						}
						Set<Abstraction> res = new HashSet<Abstraction>();

						//we cannot determine the callargs here, so just add unconditional ones:
						source.addCurrentCallArgs(new HashMap<Integer, Local>());
						
						// if the returned value is tainted - taint values from return statements
						if (src instanceof DefinitionStmt) {
							DefinitionStmt defnStmt = (DefinitionStmt) src;
							Value leftOp = defnStmt.getLeftOp();
							if (leftOp.equals(source.getAccessPath().getPlainValue())) {
								// look for returnStmts:
								for (Unit u : method.getActiveBody().getUnits()) {
									if (u instanceof ReturnStmt) {
										ReturnStmt rStmt = (ReturnStmt) u;
										if (pathTracking == PathTrackingMethod.ForwardTracking)
											res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(rStmt.getOp()), source.getSource()));
										else
											res.add(source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(rStmt.getOp())));
									}
								}
							}
						}

						// easy: static
						if (source.getAccessPath().isStaticFieldRef()) {
							res.add(source);
						}

						// checks: this/fields

						Value sourceBase = source.getAccessPath().getPlainValue();
						Stmt iStmt = (Stmt) src;
						Local thisL = null;
						if (!method.isStatic()) {
							thisL = method.getActiveBody().getThisLocal();
						}
						// TODO: wie NullConstant
						if (thisL != null) {
							InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
							if (iIExpr.getBase().equals(sourceBase)) {
								// there is only one case in which this must be added, too: if the caller-Method has the same thisLocal - check this:

								boolean param = false;
								// check if it is not one of the params (then we have already fixed it)
								for (int i = 0; i < method.getParameterCount(); i++) {
									if (iStmt.getInvokeExpr().getArg(i).equals(sourceBase)) {
										param = true;
									}
								}
								if (!param) {

									if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
										if (pathTracking == PathTrackingMethod.ForwardTracking)
											res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(thisL), source.getSource()));
										else
											res.add(source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(thisL)));
									}
								}
							}
							// TODO: maybe params?

						}

						return res;
					}

				};
			}

			@Override
			public FlowFunction<Abstraction> getReturnFlowFunction(Unit callSite, final SootMethod callee, Unit exitStmt, final Unit retSite) {
				final Stmt stmt = (Stmt) callSite;
				final InvokeExpr ie = stmt.getInvokeExpr();
				final List<Value> callArgs = ie.getArgs();
				final List<Value> paramLocals = new ArrayList<Value>();
				for (int i = 0; i < callee.getParameterCount(); i++) {
					paramLocals.add(callee.getActiveBody().getParameterLocal(i));
				}

				return new FlowFunction<Abstraction>() {

					@Override
					public Set<Abstraction> computeTargets(Abstraction source) {
						if (taintWrapper != null && taintWrapper.supportsBackwardWrapping() && taintWrapper.supportsTaintWrappingForClass(ie.getMethod().getDeclaringClass())) {
							// taint is propagated in CallToReturnFunction, so we do not need any taint here:
							return Collections.emptySet();
						}
						if (source.equals(zeroValue)) {
							return Collections.emptySet();
						}

						// check if we handle the correct return function by comparing the arguments - otherwise we return the empty set
						if (source.getcurrentArgs() != null) {
							for (Entry<Integer, Local> entry : source.getcurrentArgs().entrySet()) {
								if (entry.getKey() >= callArgs.size()) {
									System.out.println("wrong size for call " + stmt + "( size: "+ callArgs.size() +") this is what I got:" + entry.getKey() + " " + entry.getValue() +  " in "+ source.getcurrentArgs());
									return Collections.emptySet();
								}
								if (!callArgs.get(entry.getKey()).equals(entry.getValue())) {
									System.out.println("arguments do not match:" + callArgs.get(entry.getKey()) + " " + entry.getValue());
									return Collections.emptySet();
								}
							}
						}
						source.popCurrentCallArgs();

						Value base = source.getAccessPath().getPlainValue();
						Set<Abstraction> res = new HashSet<Abstraction>();
						// if taintedobject is instancefieldRef we have to check if the object is delivered..
						if (source.getAccessPath().isInstanceFieldRef()) {

							// second, they might be changed as param - check this

							// first, instancefieldRefs must be propagated if they come from the same class:
							if (!callee.isStatic() && callee.getActiveBody().getThisLocal().equals(base) && ie instanceof InstanceInvokeExpr) {
								InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
								if (pathTracking == PathTrackingMethod.ForwardTracking)
									res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(vie.getBase()), source.getSource()));
								else
									res.add(source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(vie.getBase())));
							}
						}

						// check if param is tainted:
						for (int i = 0; i < callArgs.size(); i++) {
							if (paramLocals.get(i).equals(base)) {
								if (pathTracking == PathTrackingMethod.ForwardTracking)
									res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(callArgs.get(i)), source.getSource()));
								else
									res.add(source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(callArgs.get(i))));
								// if abs. contains "neutral" -> this is the case :/ @LinkedListNegativeTest
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
			public FlowFunction<Abstraction> getCallToReturnFlowFunction(Unit call, final Unit returnSite) {
				// special treatment for native methods:
				if (call instanceof Stmt) {
					final Stmt iStmt = (Stmt) call;
					final List<Value> callArgs = iStmt.getInvokeExpr().getArgs();

					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							Set<Abstraction> res = new HashSet<Abstraction>();

							// only pass source if the source is not created by this methodcall
							if (!(iStmt instanceof DefinitionStmt) || !((DefinitionStmt) iStmt).getLeftOp().equals(source.getAccessPath().getPlainValue())) {
								res.add(source);
							}
							// taintwrapper (might be very conservative/inprecise...)
							if (taintWrapper != null && taintWrapper.supportsBackwardWrapping() && taintWrapper.supportsTaintWrappingForClass(iStmt.getInvokeExpr().getMethod().getDeclaringClass())) {
								if (iStmt instanceof DefinitionStmt && ((DefinitionStmt) iStmt).getLeftOp().equals(source.getAccessPath().getPlainValue())) {
									List<Value> vals = taintWrapper.getBackwardTaintsForMethod(iStmt);
									if (vals != null) {
										for (Value val : vals) {
											if (pathTracking == PathTrackingMethod.ForwardTracking)
												res.add(new AbstractionWithPath(new EquivalentValue(val), source.getSource(), false));
											else
												res.add(source.deriveNewAbstraction(new EquivalentValue(val), false));
										}
									}
								}
							}
							if (iStmt.getInvokeExpr().getMethod().isNative()) {
								if (callArgs.contains(source.getAccessPath().getPlainValue()) || (iStmt instanceof DefinitionStmt && ((DefinitionStmt) iStmt).getLeftOp().equals(source.getAccessPath().getPlainValue()))) {
									// java uses call by value, but fields of complex objects can be changed (and tainted), so use this conservative approach:
									// taint all params if one param or return value is tainted)
									NativeCallHandler ncHandler = new DefaultNativeCallHandler();
									res.addAll(ncHandler.getTaintedValuesForBackwardAnalysis(iStmt, source, callArgs, interproceduralCFG().getMethodOf(returnSite)));
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

}
