/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow.problems;

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

import soot.ArrayType;
import soot.IntType;
import soot.Local;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.solver.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.IInfoflowSolver;
import soot.jimple.infoflow.solver.functions.SolverCallToReturnFlowFunction;
import soot.jimple.infoflow.solver.functions.SolverNormalFlowFunction;
import soot.jimple.infoflow.solver.functions.SolverReturnFlowFunction;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.source.SourceInfo;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.infoflow.util.BaseSelector;

/**
 * class which contains the flow functions for the backwards solver. This is required for on-demand alias analysis.
 */
public class BackwardsInfoflowProblem extends AbstractInfoflowProblem {
	private IInfoflowSolver fSolver;

	public void setTaintWrapper(ITaintPropagationWrapper wrapper) {
		taintWrapper = wrapper;
	}

	public BackwardsInfoflowProblem(ISourceSinkManager sourceSinkManager) {
		this(new BackwardsInfoflowCFG(), sourceSinkManager);
	}

	public BackwardsInfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg,
			ISourceSinkManager sourceSinkManager) {
		super(icfg, sourceSinkManager);
	}

	public void setForwardSolver(IInfoflowSolver forwardSolver) {
		fSolver = forwardSolver;
	}
	
	@Override
	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			/**
			 * Computes the aliases for the given statement
			 * @param def The definition statement from which to extract
			 * the alias information
			 * @param d1 The abstraction at the method's start node
			 * @param source The source abstraction of the alias search
			 * from before the current statement
			 * @return The set of abstractions after the current statement
			 */
			private Set<Abstraction> computeAliases
					(final DefinitionStmt defStmt, Abstraction d1, Abstraction source) {
				assert !source.getAccessPath().isEmpty();
				
				final Set<Abstraction> res = new HashSet<Abstraction>();
				final Value leftValue = BaseSelector.selectBase(defStmt.getLeftOp(), true);
				
				// A backward analysis looks for aliases of existing taints and thus
				// cannot create new taints out of thin air
				if (source.equals(zeroValue))
					return Collections.emptySet();

				// Check whether the left side of the assignment matches our
				// current taint abstraction
				final boolean leftSideMatches = baseMatches(leftValue, source);
				if (!leftSideMatches)
					res.add(source);
				
				// Is the left side overwritten completely?
				if (leftSideMatches) {
					// If we have an assignment to the base local of the current taint,
					// all taint propagations must be below that point, so this is the
					// right point to turn around.
					Abstraction fabs = getForwardAbstraction(source);
					for (Unit u : interproceduralCFG().getPredsOf(defStmt))
						fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, fabs));
				}
				
				if (defStmt instanceof AssignStmt) {
					// Get the right side of the assignment
					final AssignStmt assignStmt = (AssignStmt) defStmt;
					final Value rightValue = BaseSelector.selectBase(assignStmt.getRightOp(), false);

					// Is the left side overwritten completely?
					if (leftSideMatches) {
						// Termination shortcut: If the right side is a value we do not track,
						// we can stop here.
						if (!(rightValue instanceof Local || rightValue instanceof FieldRef)) {
							return Collections.emptySet();
						}
					}
					
					// If we assign a constant, there is no need to track the right side
					// any further or do any forward propagation since constants cannot
					// carry taint.
					if (rightValue instanceof Constant)
						return res;

					// We only process heap objects. Binary operations can only
					// be performed on primitive objects.
					if (rightValue instanceof BinopExpr)
						return res;

					// If the tainted value 'b' is assigned to variable 'a' and 'a'
					// is a heap object, we must also look for aliases of 'a' upwards
					// from the current statement.
					if (rightValue instanceof InstanceFieldRef) {
						InstanceFieldRef ref = (InstanceFieldRef) rightValue;
						if (source.getAccessPath().isInstanceFieldRef()
								&& ref.getBase().equals(source.getAccessPath().getPlainValue())
								&& ref.getField().equals(source.getAccessPath().getFirstField())) {
							Abstraction abs = source.deriveNewAbstraction(leftValue, true,
									source.getAccessPath().getType());
							res.add(abs);
						}
					}
					else if (enableStaticFields && rightValue instanceof StaticFieldRef) {
						StaticFieldRef ref = (StaticFieldRef) rightValue;
						if (source.getAccessPath().isStaticFieldRef()
								&& ref.getField().equals(source.getAccessPath().getFirstField())) {
							Abstraction abs = source.deriveNewAbstraction(leftValue, true,
									source.getAccessPath().getType());
							res.add(abs);
						}
					}
					else if (rightValue.equals(source.getAccessPath().getPlainValue())) {
						Type newType = source.getAccessPath().getType();
						if (leftValue instanceof ArrayRef)
							newType = ArrayType.v(newType, 1);
						else if (assignStmt.getRightOp() instanceof ArrayRef)
							newType = ((ArrayType) newType).getArrayElementType();
	
						// Special type handling for certain operations
						if (assignStmt.getRightOp() instanceof LengthExpr) {
							assert source.getAccessPath().getType() instanceof ArrayType;
							newType = IntType.v();
						}
								
						Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue
								(leftValue, newType), defStmt);
						res.add(abs);
					}

					// If we have the tainted value on the left side of the assignment,
					// we also have to look or aliases of the value on the right side of
					// the assignment.
					if (rightValue instanceof Local
							|| rightValue instanceof FieldRef) {
						boolean addRightValue = false;
						boolean cutFirstField = false;
						Type targetType = null;
						
						// if both are fields, we have to compare their fieldName via equals and their bases via PTS
						targetType = source.getAccessPath().getType();
						if (leftValue instanceof InstanceFieldRef) {
							InstanceFieldRef leftRef = (InstanceFieldRef) leftValue;
							if (leftRef.getBase().equals(source.getAccessPath().getPlainLocal())) {
								if (source.getAccessPath().isInstanceFieldRef()) {
									if (leftRef.getField().equals(source.getAccessPath().getFirstField())) {
										addRightValue = true;
										cutFirstField = true;
									}
								}
							}
							// indirect taint propagation:
							// if leftValue is local and source is instancefield of this local:
						} else if (leftValue instanceof Local && source.getAccessPath().isInstanceFieldRef()) {
							Local base = source.getAccessPath().getPlainLocal(); // ?
							if (leftValue.equals(base)) {
								addRightValue = true;
							}
						} else if (leftValue instanceof ArrayRef) {
							Local leftBase = (Local) ((ArrayRef) leftValue).getBase();
							if (leftBase.equals(source.getAccessPath().getPlainValue())) {
								addRightValue = true;
								assert source.getAccessPath().getType() instanceof ArrayType;
							}
							// generic case, is true for Locals, ArrayRefs that are equal etc..
						} else if (leftValue.equals(source.getAccessPath().getPlainValue())) {
							addRightValue = true;
						}
						
						// if one of them is true -> add rightValue
						if (addRightValue) {
							if (targetType != null) {
								// Special handling for some operations
								if (assignStmt.getRightOp() instanceof ArrayRef && ! (targetType instanceof ArrayType))
									targetType = ArrayType.v(targetType, 1);
								else if (leftValue instanceof ArrayRef) {
									assert source.getAccessPath().getType() instanceof ArrayType;
									targetType = ((ArrayType) targetType).getArrayElementType();
								}
							}
							
							// Special type handling for certain operations
							if (assignStmt.getRightOp() instanceof LengthExpr)
								targetType = null;
							else if (assignStmt.getRightOp() instanceof CastExpr)
								targetType = null;
							
							Abstraction newAbs = source.deriveNewAbstraction(rightValue, cutFirstField,
									targetType);
							res.add(newAbs);
						}
					}
				}
				else if (defStmt instanceof IdentityStmt)
					res.add(source);

				// Trigger the forward solver with every newly-created alias
				// on a static field. Since we don't have a fixed turn-around
				// point for static fields, we turn around on every use.
				if (enableStaticFields)
					for (Abstraction newAbs : res)
						if (newAbs.getAccessPath().isStaticFieldRef() && newAbs != source) {
							Abstraction fabs = getForwardAbstraction(newAbs);
							for (Unit u : interproceduralCFG().getPredsOf(defStmt))
								fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, fabs));
						}

				return res;
			}
			
			/**
			 * Computes the abstraction to be injected into the forward solver
			 * @param bwAbs The backward abstraction from the alias search
			 * @return The new forward abstraction to be injected into the other
			 * solver
			 */
			private Abstraction getForwardAbstraction(Abstraction bwAbs) {
				if (flowSensitiveAliasing)
					return bwAbs;
				if (bwAbs.isAbstractionActive())
					return bwAbs;
				
				return bwAbs.getActiveCopy();
			}

			@Override
			public FlowFunction<Abstraction> getNormalFlowFunction(final Unit src, final Unit dest) {
				
				if (src instanceof DefinitionStmt) {
					final DefinitionStmt defStmt = (DefinitionStmt) src;

					return new SolverNormalFlowFunction() {

						@Override
						public Set<Abstraction> computeTargets(Abstraction d1, Abstraction source) {
							if (source.equals(zeroValue))
								return Collections.emptySet();
							assert !source.isAbstractionActive() || !flowSensitiveAliasing;
							Set<Abstraction> res = computeAliases(defStmt, d1, source);
							
							// If the next statement assigns the base of the tainted value,
							// we look ahead with the alias search. This fixes the problem
							// that the first statement of a method never ends up in "src".
							if (dest instanceof DefinitionStmt) {
								DefinitionStmt defStmt = (DefinitionStmt) dest;
								for (Abstraction newAbs : res)
									if (baseMatches(defStmt.getLeftOp(), newAbs)) {
										Abstraction fabs = getForwardAbstraction(newAbs);
										for (Unit u : interproceduralCFG().getPredsOf(defStmt))
											fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, fabs));
									}
 							}

							return res;
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
				final List<Value> paramLocals = new ArrayList<Value>(dest.getParameterCount());
				for (int i = 0; i < dest.getParameterCount(); i++)
					paramLocals.add(dest.getActiveBody().getParameterLocal(i));

				final SourceInfo sourceInfo = sourceSinkManager != null
						? sourceSinkManager.getSourceInfo((Stmt) src, interproceduralCFG()) : null;
				final boolean isSink = sourceSinkManager != null
						? sourceSinkManager.isSink(stmt, interproceduralCFG()) : false;

				return new FlowFunction<Abstraction>() {

					public Set<Abstraction> computeTargets(Abstraction source) {
						if (source.equals(zeroValue))
							return Collections.emptySet();
						
						//if we do not have to look into sources or sinks:
						if (!inspectSources && sourceInfo != null)
							return Collections.emptySet();
						if (!inspectSinks && isSink)
							return Collections.emptySet();

						// taint is propagated in CallToReturnFunction, so we do not need any taint here if it is exclusive:
						if(taintWrapper != null && taintWrapper.isExclusive(stmt, source.getAccessPath()))
							return Collections.emptySet();
						
						Set<Abstraction> res = new HashSet<Abstraction>();
												
						// if the returned value is tainted - taint values from return statements
						if (src instanceof DefinitionStmt) {
							DefinitionStmt defnStmt = (DefinitionStmt) src;
							Value leftOp = defnStmt.getLeftOp();
							if (leftOp.equals(source.getAccessPath().getPlainValue())) {
								// look for returnStmts:
								for (Unit u : dest.getActiveBody().getUnits()) {
									if (u instanceof ReturnStmt) {
										ReturnStmt rStmt = (ReturnStmt) u;
										if (rStmt.getOp() instanceof Local
												|| rStmt.getOp() instanceof FieldRef) {
											Abstraction abs = source.deriveNewAbstraction
													(source.getAccessPath().copyWithNewValue(rStmt.getOp(), null), (Stmt) src);
											assert abs != source;		// our source abstraction must be immutable
											res.add(abs);
										}
									}
								}
							}
						}

						// easy: static
						if (enableStaticFields && source.getAccessPath().isStaticFieldRef())
							res.add(source);

						// checks: this/fields
						Value sourceBase = source.getAccessPath().getPlainValue();
						if (!dest.isStatic()) {
							Local thisL = dest.getActiveBody().getThisLocal();
							InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
							if (iIExpr.getBase().equals(sourceBase)
									&& (hasCompatibleTypesForCall(source.getAccessPath(), dest.getDeclaringClass()))) {
								boolean param = false;
								// check if it is not one of the params (then we have already fixed it)
								for (int i = 0; i < dest.getParameterCount(); i++) {
									if (stmt.getInvokeExpr().getArg(i).equals(sourceBase)) {
										param = true;
										break;
									}
								}
								if (!param) {
									Abstraction abs = source.deriveNewAbstraction
											(source.getAccessPath().copyWithNewValue(thisL), (Stmt) src);
									res.add(abs);
								}
							}
						}
						
						// Map the parameter values into the callee
						if(!dest.getName().equals("<clinit>")) {
							assert dest.getParameterCount() == callArgs.size();
							// check if param is tainted:
							for (int i = 0; i < callArgs.size(); i++) {
								if (callArgs.get(i).equals(source.getAccessPath().getPlainLocal())) {
									Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue
											(paramLocals.get(i)), stmt);
									res.add(abs);
								}
							}
						}
						return res;
					}
				};
			}

			@Override
			public FlowFunction<Abstraction> getReturnFlowFunction(final Unit callSite, final SootMethod callee,
					final Unit exitStmt, final Unit retSite) {
				final ReturnStmt returnStmt = (exitStmt instanceof ReturnStmt) ? (ReturnStmt) exitStmt : null;
				
				return new SolverReturnFlowFunction() {
					
					@Override
					public Set<Abstraction> computeTargets(Abstraction source,
							Set<Abstraction> callerD1s) {
						if (source.equals(zeroValue))
							return Collections.emptySet();
						
						// If we have no caller, we have nowhere to propagate. This
						// can happen when leaving the main method.
						if (callSite == null)
							return Collections.emptySet();
						
						// easy: static
						if (enableStaticFields && source.getAccessPath().isStaticFieldRef()) {
							registerActivationCallSite(callSite, callee, source);
							return Collections.singleton(source);
						}

						final Value sourceBase = source.getAccessPath().getPlainLocal();
						Set<Abstraction> res = new HashSet<Abstraction>();

						// if we have a returnStmt we have to look at the returned value:
						if (returnStmt != null && callSite instanceof DefinitionStmt) {
							DefinitionStmt defnStmt = (DefinitionStmt) callSite;
							Value leftOp = defnStmt.getLeftOp();
							
							if (leftOp == source.getAccessPath().getPlainValue()) {
								Abstraction abs = source.deriveNewAbstraction
										(source.getAccessPath().copyWithNewValue(leftOp), (Stmt) exitStmt);
								res.add(abs);
								registerActivationCallSite(callSite, callee, abs);
							}
						}
						
						// check one of the call params are tainted (not if simple type)
						{
						Value originalCallArg = null;
						for (int i = 0; i < callee.getParameterCount(); i++) {
							if (callee.getActiveBody().getParameterLocal(i) == sourceBase) 
								if (callSite instanceof Stmt) {
									Stmt iStmt = (Stmt) callSite;
									originalCallArg = iStmt.getInvokeExpr().getArg(i);
									
									// If this is a constant parameter, we can safely ignore it
									if (!AccessPath.canContainValue(originalCallArg))
										continue;

									Abstraction abs = source.deriveNewAbstraction
											(source.getAccessPath().copyWithNewValue(originalCallArg), (Stmt) exitStmt);
									res.add(abs);
									registerActivationCallSite(callSite, callee, abs);
								}
						}
						}
						
						{
						if (!callee.isStatic()) {
							Local thisL = callee.getActiveBody().getThisLocal();
							if (thisL == sourceBase && 	hasCompatibleTypesForCall
									(source.getAccessPath(), callee.getDeclaringClass())) {
								boolean param = false;
								// check if it is not one of the params (then we have already fixed it)
								for (int i = 0; i < callee.getParameterCount(); i++) {
									if (callee.getActiveBody().getParameterLocal(i) == sourceBase) {
										param = true;
										break;
									}
								}
								if (!param) {
									if (callSite instanceof Stmt) {
										Stmt stmt = (Stmt) callSite;
										if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
											InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
											Abstraction abs = source.deriveNewAbstraction
													(source.getAccessPath().copyWithNewValue(iIExpr.getBase()), stmt);
											res.add(abs);
											registerActivationCallSite(callSite, callee, abs);
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

					return new SolverCallToReturnFlowFunction() {
						@Override
						public Set<Abstraction> computeTargets(Abstraction d1, Abstraction source) {
							if (source.equals(zeroValue))
								return Collections.emptySet();

							// only pass source if the source is not created by this method call
							if (iStmt instanceof DefinitionStmt && ((DefinitionStmt) iStmt).getLeftOp().equals
									(source.getAccessPath().getPlainValue())){
								//terminates here, but we have to start a forward pass to consider all method calls:
								Abstraction fabs = getForwardAbstraction(source);
								for (Unit u : interproceduralCFG().getPredsOf(iStmt))
									fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, fabs));
								return Collections.emptySet();
							}
							else {
								// If the return site matches on an abstraction we pass on, we may have to turn around
								// that the first statement of a method never ends up in "src".
								if (returnSite instanceof DefinitionStmt) {
									DefinitionStmt defStmt = (DefinitionStmt) returnSite;
									if (baseMatches(defStmt.getLeftOp(), source)
											/*&& triggerInaktiveTaintOrReverseFlow(defStmt.getLeftOp(), source)*/) {
										Abstraction fabs = getForwardAbstraction(source);
										for (Unit u : interproceduralCFG().getPredsOf(defStmt))
											fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, u, fabs));
									}
	 							}
								return Collections.singleton(source);
							}
						}
					};
				}
				return Identity.v();
			}
		};
	}

}
