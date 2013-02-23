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
import java.util.Set;
import java.util.Map.Entry;

import soot.Local;
import soot.NullType;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionWithPath;
import soot.jimple.infoflow.heros.InfoflowSolver;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.source.SourceSinkManager;
import soot.jimple.infoflow.util.BaseSelector;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

public class InfoflowProblem extends AbstractInfoflowProblem {

	InfoflowSolver bSolver;
	private final static boolean DEBUG = false;
	final SourceSinkManager sourceSinkManager;
	Abstraction zeroValue = null;
	
	/**
	 * Computes the taints produced by a taint wrapper object
	 * @param iStmt The call statement the taint wrapper shall check for well-
	 * known methods that introduce black-box taint propagation 
	 * @param callArgs The actual parameters with which the method in invoked
	 * @param source The taint source
	 * @return The taints computed by the wrapper
	 */
	private Set<Abstraction> computeWrapperTaints
			(final Stmt iStmt,
			final List<Value> callArgs,
			Abstraction source) {
		Set<Abstraction> res = new HashSet<Abstraction>();
		if(taintWrapper == null || !taintWrapper.supportsTaintWrappingForClass(iStmt.getInvokeExpr().getMethod().getDeclaringClass()))
			return Collections.emptySet();
		
		int taintedPos = -1;
		for(int i=0; i< callArgs.size(); i++){
			if(source.getAccessPath().isLocal() && callArgs.get(i).equals(source.getAccessPath().getPlainValue())){
				taintedPos = i;
				break;
			}
		}
		Value taintedBase = null;
		if(iStmt.getInvokeExpr() instanceof InstanceInvokeExpr){
			InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
			if(iiExpr.getBase().equals(source.getAccessPath().getPlainValue())){
				if(source.getAccessPath().isLocal()){
					taintedBase = iiExpr.getBase();
				}
				else if(source.getAccessPath().isInstanceFieldRef()){
					// The taint refers to the actual type of the field, not the formal type,
					// so we must check whether we have the tainted field at all
					SootClass callerClass = interproceduralCFG().getMethodOf(iStmt).getDeclaringClass();
					if (callerClass.getFields().contains(source.getAccessPath().getField()))
						taintedBase = new JInstanceFieldRef(iiExpr.getBase(),
								callerClass.getFieldByName(source.getAccessPath().getField().getName()).makeRef());
				}
			}
			
			// For the moment, we don't implement static taints on wrappers
			if(source.getAccessPath().isStaticFieldRef()){
				//TODO
			}
		}
			
		List<Value> vals = taintWrapper.getTaintsForMethod(iStmt, taintedPos, taintedBase);
		if(vals != null)
			for (Value val : vals)
				if (pathTracking == PathTrackingMethod.ForwardTracking)
					res.add(new AbstractionWithPath(val, source.getSource(),
							((AbstractionWithPath) source).getPropagationPath(),
							iStmt, false));
				else
					res.add(source.deriveNewAbstraction(val, false));
					//res.add(new Abstraction(val, source.getSource(), false));
		return res;
	}

	/**
	 * Checks whether a taint wrapper is exclusive for a specific invocation statement
	 * @param iStmt The call statement the taint wrapper shall check for well-
	 * known methods that introduce black-box taint propagation 
	 * @param callArgs The actual parameters with which the method in invoked
	 * @param source The taint source
	 * @return True if the wrapper is exclusive, otherwise false
	 */
	private boolean isWrapperExclusive
			(final Stmt iStmt,
			final List<Value> callArgs,
			Abstraction source) {
		if(taintWrapper == null || !taintWrapper.supportsTaintWrappingForClass(iStmt.getInvokeExpr().getMethod().getDeclaringClass()))
			return false;
		
		int taintedPos = -1;
		for(int i=0; i< callArgs.size(); i++){
			if(source.getAccessPath().isLocal() && callArgs.get(i).equals(source.getAccessPath().getPlainValue())){
				taintedPos = i;
				break;
			}
		}
		Value taintedBase = null;
		if(iStmt.getInvokeExpr() instanceof InstanceInvokeExpr){
			InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
			if(iiExpr.getBase().equals(source.getAccessPath().getPlainValue())){
				if(source.getAccessPath().isLocal()){
					taintedBase = iiExpr.getBase();
				}else if(source.getAccessPath().isInstanceFieldRef()){
					// The taint refers to the actual type of the field, not the formal type,
					// so we must check whether we have the tainted field at all
					SootClass callerClass = interproceduralCFG().getMethodOf(iStmt).getDeclaringClass();
					if (callerClass.getFields().contains(source.getAccessPath().getField()))
						taintedBase = new JInstanceFieldRef(iiExpr.getBase(),
								callerClass.getFieldByName(source.getAccessPath().getField().getName()).makeRef());
				}
			}
			if(source.getAccessPath().isStaticFieldRef()){
				//TODO
			}
		}
			
		return taintWrapper.isExclusive(iStmt, taintedPos, taintedBase);
	}
	
	@Override
	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			/**
			 * Creates a new taint abstraction for the given value
			 * @param src The source statement from which the taint originated
			 * @param targetValue The target value that shall now be tainted
			 * @param source The incoming taint abstraction from the source
			 * @param taintSet The taint set to which to add all newly produced
			 * @param keepAllFieldTaintStar defines if the field taint should be kept or whole object is tainted
			 * taints
			 */
			private void addTaintViaStmt
					(final Unit src,
					final Value targetValue,
					Abstraction source,
					Set<Abstraction> taintSet,
					boolean keepAllFieldTaintStar,
					boolean forceFields) {
				taintSet.add(source);
				if (pathTracking == PathTrackingMethod.ForwardTracking)
					taintSet.add(new AbstractionWithPath(targetValue,
							source.getSource(),
							((AbstractionWithPath) source).getPropagationPath(),
							src, keepAllFieldTaintStar && source.getAccessPath().isOnlyFieldsTainted()));
				else
					taintSet.add(source.deriveNewAbstraction(targetValue, keepAllFieldTaintStar && source.getAccessPath().isOnlyFieldsTainted()));

					if (triggerReverseFlow(targetValue)) {
						// call backwards-check:
						Unit predUnit = getUnitBefore(src);
						Abstraction newAbs = source.deriveNewAbstraction(targetValue, (forceFields) ? true : keepAllFieldTaintStar && source.getAccessPath().isOnlyFieldsTainted());
						bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(newAbs, predUnit, newAbs));

					}
			}

			@Override
			public FlowFunction<Abstraction> getNormalFlowFunction(final Unit src, final Unit dest) {
				// If we compute flows on parameters, we create the initial
				// flow fact here
				if (computeParamFlows && src instanceof IdentityStmt
						&& isInitialMethod(interproceduralCFG().getMethodOf(src))) {
					final IdentityStmt is = (IdentityStmt) src;
					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							if (stopAfterFirstFlow && !results.isEmpty())
								return Collections.emptySet();
							if (is.getRightOp() instanceof ParameterRef) {
								if (pathTracking != PathTrackingMethod.NoTracking) {
									List<Unit> empty = Collections.emptyList();
									Abstraction abs = new AbstractionWithPath(is.getLeftOp(),
										is.getRightOp(),
										empty,
										is, false);
									return Collections.singleton(abs);
								}
								else
									return Collections.singleton
										(new Abstraction(is.getLeftOp(),
										is.getRightOp(), false));
							}
							return Collections.singleton(source);
						}
					};
				}
				// taint is propagated with assignStmt
				else if (src instanceof AssignStmt) {
					AssignStmt assignStmt = (AssignStmt) src;
					Value right = assignStmt.getRightOp();
					Value left = assignStmt.getLeftOp();

					final Value leftValue = BaseSelector.selectBase(left, false);
					final Set<Value> rightVals = BaseSelector.selectBaseList(right, true);

					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							if (stopAfterFirstFlow && !results.isEmpty())
								return Collections.emptySet();

							boolean addLeftValue = false;
							boolean keepAllFieldTaintStar = true;
							boolean forceFields = false;
							Set<Abstraction> res = new HashSet<Abstraction>();
							// shortcuts:
							// on NormalFlow taint cannot be created
							if (source.equals(zeroValue)) {
								return Collections.emptySet();
							}
							if(src.toString().contains("a.<soot.jimple.infoflow.test.HeapTestCode$A: java.lang.String b> = tai")){
								System.out.println("salad");
							}
							
							for (Value rightValue : rightVals) {
								// check if static variable is tainted (same name, same class)
								if (source.getAccessPath().isStaticFieldRef()) {
									if (rightValue instanceof StaticFieldRef) {
										StaticFieldRef rightRef = (StaticFieldRef) rightValue;
										if (source.getAccessPath().getField().equals(rightRef.getField())) {
											addLeftValue = true;
//											keepAllFieldTaintStar = false;
										}
									}
								} else {
									// if both are fields, we have to compare their fieldName via equals and their bases via PTS
									// might happen that source is local because of max(length(accesspath)) == 1
									if (rightValue instanceof InstanceFieldRef) {
										InstanceFieldRef rightRef = (InstanceFieldRef) rightValue;
										Local rightBase = (Local) rightRef.getBase();
										Local sourceBase =  source.getAccessPath().getPlainLocal();
										if (rightBase.equals(sourceBase)) {
											if (source.getAccessPath().isInstanceFieldRef()) {
												if (rightRef.getField().equals(source.getAccessPath().getField())) {
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
										Local base = source.getAccessPath().getPlainLocal();
										if (rightValue.equals(base)) {
											if (leftValue instanceof Local) {
												if (pathTracking == PathTrackingMethod.ForwardTracking)
													res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(leftValue),
															source.getSource(),
															((AbstractionWithPath) source).getPropagationPath(),
															src));
												else
													res.add(source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(leftValue)));												
											} else {
												// access path length = 1 - taint entire value if left is field reference
												if (pathTracking == PathTrackingMethod.ForwardTracking)
													res.add(new AbstractionWithPath(leftValue,
															source.getSource(),
															((AbstractionWithPath) source).getPropagationPath(),
															src, true));
												else
													res.add(source.deriveNewAbstraction(leftValue, true));
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
									if (rightValue.equals(source.getAccessPath().getPlainValue())) {
										addLeftValue = true;
									}
								}
							}
							// if one of them is true -> add leftValue
							if (addLeftValue) {

								addTaintViaStmt(src, leftValue, source, res, keepAllFieldTaintStar, forceFields);

								return res; 
							}
							return Collections.singleton(source);

						}
					};

				}
				else if (returnIsSink &&  dest instanceof ReturnStmt) {
					// Returning from the main method may also count as a sink
					final ReturnStmt returnStmt = (ReturnStmt) dest;
					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							if (stopAfterFirstFlow && !results.isEmpty())
								return Collections.emptySet();

							boolean isSink = false;
							if (source.getAccessPath().isStaticFieldRef())
								isSink = source.getAccessPath().getField().equals(returnStmt.getOp()); //TODO: getOp is always Local? check
							else
								isSink = isInitialMethod(interproceduralCFG().getMethodOf(dest))
									&& source.getAccessPath().getPlainValue().equals(returnStmt.getOp());
							if (isSink) {
								if (pathTracking != PathTrackingMethod.NoTracking)
									results.addResult(returnStmt.toString(),
											source.getSource().toString(),
											((AbstractionWithPath) source).getPropagationPathAsString(interproceduralCFG()),
											interproceduralCFG().getMethodOf(returnStmt) + ": " + returnStmt.toString());
								else
									results.addResult(returnStmt.toString(),
											source.getSource().toString());
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
						//push empty arg map (no conditions):
						source.addCurrentCallArgs(new HashMap<Integer, Local>());

						if (stopAfterFirstFlow && !results.isEmpty())
							return Collections.emptySet();
						if (source.equals(zeroValue)) {
							return Collections.singleton(source);
						}
						if(isWrapperExclusive(stmt, callArgs, source)) {
							//taint is propagated in CallToReturnFunction, so we do not need any taint here:
							return Collections.emptySet();
						}

						
						Set<Abstraction> res = new HashSet<Abstraction>();

						// check if whole object is tainted or one field (happens with strings, for example:)
						if (!dest.isStatic() && ie instanceof InstanceInvokeExpr) {
							InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
							// this might be enough because every call must happen with a local variable which is tainted itself:
							if (vie.getBase().equals(source.getAccessPath().getPlainLocal())) {
								
								Abstraction abs;
								if (pathTracking == PathTrackingMethod.ForwardTracking)
									abs = new AbstractionWithPath(source.getAccessPath().copyWithNewValue(dest.getActiveBody().getThisLocal()), source.getSource(), ((AbstractionWithPath) source).getPropagationPath(), stmt);
								else
									abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(dest.getActiveBody().getThisLocal()));
								HashMap<Integer, Local> callMap = new HashMap<Integer, Local>();
								callMap.put(-1, (Local)vie.getBase());
								//pop the empty map and add new one:
								abs.popCurrentCallArgs();
								abs.addCurrentCallArgs(callMap);
								res.add(abs);

							}
						}

						// check if param is tainted:
						for (int i = 0; i < callArgs.size(); i++) {
							if (callArgs.get(i).equals(source.getAccessPath().getPlainLocal())) {
								Abstraction abs;
								if (pathTracking == PathTrackingMethod.ForwardTracking)
									abs = new AbstractionWithPath(source.getAccessPath().copyWithNewValue(paramLocals.get(i)), source.getSource(), ((AbstractionWithPath) source).getPropagationPath(), stmt);
								else
									abs =source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(paramLocals.get(i)));
								//add CallParam:
								HashMap<Integer, Local> callMap = new HashMap<Integer, Local>();
								callMap.put(i, (Local)callArgs.get(i));
								//pop the empty map and add new one:
								abs.popCurrentCallArgs();
								abs.addCurrentCallArgs(callMap);
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

			@Override
			public FlowFunction<Abstraction> getReturnFlowFunction(final Unit callSite, final SootMethod callee, final Unit exitStmt, final Unit retSite) {

				return new FlowFunction<Abstraction>() {

					@Override
					public Set<Abstraction> computeTargets(Abstraction source) {
						if (stopAfterFirstFlow && !results.isEmpty())
							return Collections.emptySet();
						if (source.equals(zeroValue)) {
							return Collections.emptySet();
						}
				
						// check arguments here, too. CFG seems to be confused when switching from/to backward:
						// check if we handle the correct return function by comparing the arguments - otherwise we return the empty set
						if (source.getcurrentArgs() != null) {
							Stmt stmt = (Stmt) callSite;
							InvokeExpr ie = stmt.getInvokeExpr();
							List<Value> callArgs = ie.getArgs();
							
							for (Entry<Integer, Local> entry : source.getcurrentArgs().entrySet()) {
								if (entry.getKey() >= callArgs.size()) {
									System.out.println("wrong size for call " + stmt + "( size: "+ callArgs.size() +") this is what I got:" + entry.getKey() + " " + entry.getValue() +  " in "+ source.getcurrentArgs());
									return Collections.emptySet();
								}
								if(entry.getKey() == -1){
									if(!(ie instanceof InstanceInvokeExpr) || !((InstanceInvokeExpr)ie).getBase().equals(entry.getValue())){
										System.out.println("base element does not fit!");
										return Collections.emptySet();
									}
								}else if (!callArgs.get(entry.getKey()).equals(entry.getValue())) {
									System.out.println("arguments do not match:" + callArgs.get(entry.getKey()) + " " + entry.getValue());
									return Collections.emptySet();
								} 
							}
						}
						source.popCurrentCallArgs();
						
						Set<Abstraction> res = new HashSet<Abstraction>();
						// if we have a returnStmt we have to look at the returned value:
						if (exitStmt instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) exitStmt;
							Value retLocal = returnStmt.getOp();

							if (callSite instanceof DefinitionStmt) {
								DefinitionStmt defnStmt = (DefinitionStmt) callSite;
								Value leftOp = defnStmt.getLeftOp();
								if (retLocal.equals(source.getAccessPath().getPlainLocal())) {
									if (pathTracking == PathTrackingMethod.ForwardTracking)
										res.add(new AbstractionWithPath(source.getAccessPath().copyWithNewValue(leftOp), source.getSource(), ((AbstractionWithPath) source).getPropagationPath(), exitStmt));
									else
										res.add(source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(leftOp)));
								}
								
								// TODO: think about it - is this necessary?
								// if(forwardbackward){
								// //call backwards-check:
								// Unit predUnit = getUnitBefore(callUnit);
								// Abstraction newAbs = source.deriveAbstraction(leftValue,
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
						Value sourceBase = source.getAccessPath().getPlainLocal();
						Value originalCallArg = null;
						for (int i = 0; i < callee.getParameterCount(); i++) {
							if (callee.getActiveBody().getParameterLocal(i).equals(sourceBase)) {
								if (callSite instanceof Stmt) {
									Stmt iStmt = (Stmt) callSite;
									originalCallArg = iStmt.getInvokeExpr().getArg(i);
									//either the param is a fieldref (not possible in jimple?) or an array Or one of its fields is tainted/all fields are tainted
									if (triggerReverseFlow(originalCallArg, source)) {
										Abstraction abs;
										if (pathTracking == PathTrackingMethod.ForwardTracking)
											abs = new AbstractionWithPath(source.getAccessPath().copyWithNewValue(originalCallArg), source.getSource(), ((AbstractionWithPath) source).getPropagationPath(), exitStmt);
										else
											abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(originalCallArg));
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
											Abstraction abs;
											if (pathTracking == PathTrackingMethod.ForwardTracking)
												abs = new AbstractionWithPath(source.getAccessPath().copyWithNewValue(iIExpr.getBase()), source.getSource(), ((AbstractionWithPath) source).getPropagationPath(), exitStmt);
											else
												abs =  source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(iIExpr.getBase()));
											res.add(abs);
											//trigger reverseFlow:
											if (triggerReverseFlow(iIExpr.getBase(), source)) {
												Unit predUnit = getUnitBefore(callSite);
												bSolver.processEdge(new PathEdge<Unit, Abstraction, SootMethod>(abs, predUnit, abs));
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
			public FlowFunction<Abstraction> getCallToReturnFlowFunction(final Unit call, final Unit returnSite) {
				// special treatment for native methods:
				if (call instanceof Stmt) {
					final Stmt iStmt = (Stmt) call;
					final List<Value> callArgs = iStmt.getInvokeExpr().getArgs();

					return new FlowFunction<Abstraction>() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction source) {
							if (stopAfterFirstFlow && !results.isEmpty())
								return Collections.emptySet();

							Set<Abstraction> res = new HashSet<Abstraction>();
							res.add(source);
							res.addAll(computeWrapperTaints(iStmt, callArgs, source));
							
							if (iStmt.getInvokeExpr().getMethod().isNative()) {
								if (callArgs.contains(source.getAccessPath().getPlainValue())) {
									// java uses call by value, but fields of complex objects can be changed (and tainted), so use this conservative approach:
									res.addAll(ncHandler.getTaintedValues(iStmt, source, callArgs));
								}
							}

							if (iStmt instanceof JAssignStmt) {
								final JAssignStmt stmt = (JAssignStmt) iStmt;

								if (sourceSinkManager.isSource(stmt)) {
									if (DEBUG)
										System.out.println("Found source: " + stmt.getInvokeExpr().getMethod());
									if (pathTracking == PathTrackingMethod.ForwardTracking)
										res.add(new AbstractionWithPath(stmt.getLeftOp(),
												stmt.getInvokeExpr(), 
												((AbstractionWithPath) source).getPropagationPath(),
												call, false));
									else
										res.add(new Abstraction(stmt.getLeftOp(),
												stmt.getInvokeExpr(), false));
									res.remove(zeroValue);
								}
							}

							// if we have called a sink we have to store the path from the source - in case one of the params is tainted!
							if (sourceSinkManager.isSink(iStmt)) {
								boolean taintedParam = false;
								for (int i = 0; i < callArgs.size(); i++) {
									if (callArgs.get(i).equals(source.getAccessPath().getPlainLocal())) {
										taintedParam = true;
										break;
									}
								}

								if (taintedParam) {
									if (pathTracking != PathTrackingMethod.NoTracking)
										results.addResult(iStmt.getInvokeExpr().getMethod().toString(),
												source.getSource().toString(),
												((AbstractionWithPath) source).getPropagationPathAsString(interproceduralCFG()),
												interproceduralCFG().getMethodOf(call) + ": " + call.toString());
									else
										results.addResult(iStmt.getInvokeExpr().getMethod().toString(),
												source.getSource().toString());
								}
								// if the base object which executes the method is tainted the sink is reached, too.
								if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
									InstanceInvokeExpr vie = (InstanceInvokeExpr) iStmt.getInvokeExpr();
									if (vie.getBase().equals(source.getAccessPath().getPlainValue())) {
										if (pathTracking != PathTrackingMethod.NoTracking)
											results.addResult(iStmt.getInvokeExpr().getMethod().toString(),
													source.getSource().toString(),
													((AbstractionWithPath) source).getPropagationPathAsString(interproceduralCFG()),
													interproceduralCFG().getMethodOf(call) + ": " + call.toString());
										else
											results.addResult(iStmt.getInvokeExpr().getMethod().toString(),
													source.getSource().toString());
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

	public InfoflowProblem(SourceSinkManager mySourceSinkManager, Set<Unit> analysisSeeds) {
	    super(new JimpleBasedInterproceduralCFG());
	    this.sourceSinkManager = mySourceSinkManager;
	    this.initialSeeds.addAll(analysisSeeds);
    }

    @Override
	public Abstraction createZeroValue() {
		if (zeroValue == null) {
			zeroValue = this.pathTracking == PathTrackingMethod.NoTracking ?
				new Abstraction(new JimpleLocal("zero", NullType.v()), null, false) :
				new AbstractionWithPath(new JimpleLocal("zero", NullType.v()), null, null, false);
		}
		return zeroValue;
	}
	
	

	public void setBackwardSolver(InfoflowSolver backwardSolver){
		bSolver = backwardSolver;
	}

	
	@Override
	public boolean autoAddZero() {
		return false;
	}
}

