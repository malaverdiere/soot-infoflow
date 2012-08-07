package soot.jimple.infoflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootMethod;
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
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.interproc.ifds.FlowFunction;
import soot.jimple.interproc.ifds.FlowFunctions;
import soot.jimple.interproc.ifds.IFDSTabulationProblem;
import soot.jimple.interproc.ifds.InterproceduralCFG;
import soot.jimple.interproc.ifds.flowfunc.Identity;
import soot.jimple.interproc.ifds.flowfunc.KillAll;
import soot.jimple.interproc.ifds.template.JimpleBasedInterproceduralCFG;
import soot.toolkits.scalar.Pair;

public class InfoflowProblem implements IFDSTabulationProblem<Unit,Pair<Value, Value>,SootMethod>{
	
	final Set <Unit> initialSeeds = new HashSet<Unit>();
	final Pair<Value, Value> zeroValue = new Pair<Value, Value>(new JimpleLocal("zero", NullType.v()),null);
	

	public FlowFunctions<Unit, Pair<Value, Value>, SootMethod> flowFunctions() {
		return new FlowFunctions<Unit,Pair<Value, Value>,SootMethod>() {

			public FlowFunction<Pair<Value, Value>> getNormalFlowFunction(Unit src, Unit dest) {
			
				if(src instanceof AssignStmt) {
					AssignStmt assignStmt = (AssignStmt) src;
					Value right = assignStmt.getRightOp();
					Value left = assignStmt.getLeftOp();
			
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
								
								//normal check for infoflow
								if(rightValue instanceof JVirtualInvokeExpr || !source.equals(zeroValue)){
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
				}
				return new FlowFunction<Pair<Value, Value>>() {

					public Set<Pair<Value, Value>> computeTargets(Pair<Value, Value> source) {
						
						int argIndex = callArgs.indexOf(source.getO1());
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
				SourceManager sourceManager = new DumbSourceManager(); 
				if(call instanceof JAssignStmt){
					final JAssignStmt stmt = (JAssignStmt) call;
					//obviously we have to check for the correct class, too (not only method)
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
}
