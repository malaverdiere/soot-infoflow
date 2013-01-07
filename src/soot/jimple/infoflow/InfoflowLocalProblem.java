package soot.jimple.infoflow;

import heros.FlowFunction;
import heros.FlowFunctions;
import heros.InterproceduralCFG;
import heros.flowfunc.Identity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.EquivalentValue;
import soot.Local;
import soot.NullType;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.nativ.DefaultNativeCallHandler;
import soot.jimple.infoflow.nativ.NativeCallHandler;
import soot.jimple.infoflow.source.DefaultSourceManager;
import soot.jimple.infoflow.source.SourceManager;
import soot.jimple.infoflow.util.LocalBaseSelector;
import soot.jimple.internal.InvokeExprBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.util.Chain;

public class InfoflowLocalProblem extends AbstractInfoflowProblem {

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

					//find appropriate leftValue:
					final Value originalLeft = left;
					left = LocalBaseSelector.selectBase(left);
					right = LocalBaseSelector.selectBase(right);

					final Value leftValue = left;
					final Value rightValue = right;

					return new FlowFunction<Abstraction>() {

						public Set<Abstraction> computeTargets(Abstraction source) {
							boolean addLeftValue = false;
							PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
							//on NormalFlow taint cannot be created
							if(source.equals(zeroValue)){
								return Collections.singleton(source);
							}
							//static variable can be identified by name and declaring class
							if (rightValue instanceof StaticFieldRef && source.getTaintedObject().getValue() instanceof StaticFieldRef) {
								StaticFieldRef rightRef = (StaticFieldRef) rightValue;
								StaticFieldRef sourceRef = (StaticFieldRef) source.getTaintedObject().getValue();
								if (rightRef.getFieldRef().name().equals(sourceRef.getFieldRef().name()) && rightRef.getFieldRef().declaringClass().equals(sourceRef.getFieldRef().declaringClass())) {
									addLeftValue = true;
								}
							}

							//if objects are equal, taint is necessary
							if (source.getTaintedObject().getValue().equals(rightValue)) {
								assert source.getTaintedObject().getValue() instanceof Local && rightValue instanceof Local;
								addLeftValue = true;
							}

							// if one of them is true -> add leftValue
							if (addLeftValue) {
								Set<Abstraction> res = new HashSet<Abstraction>();
								res.add(source);
								res.add(new Abstraction(new EquivalentValue(leftValue), source.getSource(), interproceduralCFG().getMethodOf(src)));

								SootMethod m = interproceduralCFG().getMethodOf(src);
								Chain<Local> localChain = m.getActiveBody().getLocals();
								PointsToSet ptsLeft;
								assert leftValue instanceof Local; //always true
								ptsLeft = pta.reachingObjects((Local) leftValue);								
								for (Local c : localChain) {
									PointsToSet ptsVal = pta.reachingObjects(c);
									if (c.getType().equals(leftValue.getType()) && ptsLeft.hasNonEmptyIntersection(ptsVal) && !leftValue.equals(c)) {
//											res.add(new Abstraction(new EquivalentValue(c), source.getSource(), interproceduralCFG().getMethodOf(src)));
									}
								}

								if(originalLeft instanceof InstanceFieldRef){
									Value base = ((InstanceFieldRef)originalLeft).getBase();
									Set<Value> aliases = getAliasesinMethod(m.getActiveBody().getUnits(), src, base, ((InstanceFieldRef)originalLeft).getFieldRef());
									for(Value v : aliases){
										//for normal analysis this would be enough but since this is local analysis we have to "truncate" instancefieldrefs
										//res.add(new Abstraction(new EquivalentValue(v), source.getSource(), interproceduralCFG().getMethodOf(src)));
										if(v instanceof InstanceFieldRef){
											res.add(new Abstraction(new EquivalentValue(((InstanceFieldRef)v).getBase()), source.getSource(), interproceduralCFG().getMethodOf(src)));
										} else{
											res.add(new Abstraction(new EquivalentValue(v), source.getSource(), interproceduralCFG().getMethodOf(src)));
										}
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
						if(source.equals(zeroValue)){
							return Collections.singleton(source);
						}
						
						Set<Abstraction> res = new HashSet<Abstraction>();
						List<Value> taintedParams = new LinkedList<Value>();
												
						//check if whole target object is tainted (happens with strings, for example:)
						if(!dest.isStatic() && ie instanceof InstanceInvokeExpr && source.getTaintedObject().getValue() instanceof Local){
							InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
							//this might be enough because every call must happen with a local variable which is tainted itself:
							if(vie.getBase().equals(source.getTaintedObject().getValue())){
								res.add(new Abstraction(new EquivalentValue(dest.getActiveBody().getThisLocal()), source.getSource(), dest));
							}
						}
						
						//check if param is tainted:
						for (int i = 0; i < callArgs.size(); i++) {
							if (callArgs.get(i).equals(source.getTaintedObject().getValue())) {
								Abstraction abs = new Abstraction(new EquivalentValue(paramLocals.get(i)), source.getSource(), dest);
								res.add(abs);
								taintedParams.add(paramLocals.get(i));
							}
							if(source.getTaintedObject().getValue() instanceof StaticFieldRef){
								StaticFieldRef sfr = (StaticFieldRef) source.getTaintedObject().getValue();
								if(sfr.getFieldRef().declaringClass().getType().equals((callArgs.get(i)).getType())){
									Abstraction abs = new Abstraction(new EquivalentValue(paramLocals.get(i)), source.getSource(), dest);
									res.add(abs);
									taintedParams.add(paramLocals.get(i));
								}
							}
						}
						
						
						//TODO: Move to call-to-return flow function because the call-flow-function may never be called for sinks that are excluded.						
						//if we have called a sink we have to store the path from the source - in case one of the params is tainted!
						if (sinks.contains(dest.toString())) {
							if(!taintedParams.isEmpty()){
								if (!results.containsKey(dest.toString())) {
									List<String> list = new ArrayList<String>();
									list.add(source.getSource().getValue().toString());
									results.put(dest.toString(), list);
								} else {
									results.get(dest.toString()).add(source.getSource().getValue().toString());
								}
							}
							//only for LocalAnalysis at the moment: if the base object which executes the method is tainted the sink is reached, too.
							if(ie instanceof InstanceInvokeExpr){
								InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
								if(vie.getBase().equals(source.getTaintedObject().getValue())){
									if (!results.containsKey(dest.toString())) {
										List<String> list = new ArrayList<String>();
										list.add(source.getSource().getValue().toString());
										results.put(dest.toString(), list);
									} else {
										results.get(dest.toString()).add(source.getSource().getValue().toString());
									}
								}
							}
						}
						
						//fieldRefs must be analyzed even if they are not part of the params:
						if (source.getTaintedObject().getValue() instanceof StaticFieldRef) {
							res.add(source); 	
						}
						return res;
					}
				};
			}

			public FlowFunction<Abstraction> getReturnFlowFunction(Unit callSite, SootMethod callee, Unit exitStmt, Unit retSite) {
				final SootMethod calleeMethod = callee;
				final Unit callUnit = callSite;
				final Unit exitUnit = exitStmt;

				return new FlowFunction<Abstraction>() {

					public Set<Abstraction> computeTargets(Abstraction source) {
						if(source.equals(zeroValue)){
							return Collections.singleton(source);
						}
						Set<Abstraction> res = new HashSet<Abstraction>();
						//if we have a returnStmt we have to look at the returned value:
						if (exitUnit instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) exitUnit;
							Value op = returnStmt.getOp();
							if (source.getTaintedObject().getValue().equals(op)) {
								if (callUnit instanceof DefinitionStmt) {
									DefinitionStmt defnStmt = (DefinitionStmt) callUnit;
									Value leftOp = defnStmt.getLeftOp();
									res.add(new Abstraction(new EquivalentValue(LocalBaseSelector.selectBase(leftOp)), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
								}
							}
						}
						//easy: static
						if (source.getTaintedObject().getValue() instanceof StaticFieldRef) {
							res.add(source);
						}
						if (source.getTaintedObject().getValue() instanceof Local) {
							//1. source equals thislocal:
							if(!calleeMethod.isStatic() && source.getTaintedObject().getValue().equals(calleeMethod.getActiveBody().getThisLocal())){
								//find InvokeStmt:
								InstanceInvokeExpr iIExpr = null;
								if(callUnit instanceof JInvokeStmt){
									iIExpr = (InstanceInvokeExpr) ((JInvokeStmt) callUnit).getInvokeExpr();
								}else if(callUnit instanceof AssignStmt){
									iIExpr = (InstanceInvokeExpr) ((JAssignStmt)callUnit).getInvokeExpr();
								}
								if(iIExpr != null){
									res.add(new Abstraction(new EquivalentValue(iIExpr.getBase()), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
								}	
							}
							
//							TODO can this be removed for the "Local" analysis?							
//							//reassign the ones we changed into local params:
//							PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
//							PointsToSet ptsSource = pta.reachingObjects((Local) source.getTaintedObject().getValue());
//
//							for (SootField globalField : calleeMethod.getDeclaringClass().getFields()) {
//								if (globalField.isStatic()) {
//									PointsToSet ptsGlobal = pta.reachingObjects(globalField);
//									if (ptsSource.hasNonEmptyIntersection(ptsGlobal)) {
//										res.add(new Abstraction(new EquivalentValue(Jimple.v().newStaticFieldRef(globalField.makeRef())), source.getSource(),calleeMethod));
//									}
//								} else {
//									//new approach
//									if(globalField.equals(source.getTaintedObject().getValue())){ //ggf. hier "local" taint einfügen
//										Local base = null;
//										if(callUnit instanceof JAssignStmt){
//											base = (Local) ((InstanceInvokeExpr)((JAssignStmt)callUnit).getInvokeExpr()).getBase();
//										}
//										
//										if(callUnit instanceof JInvokeStmt){
//											JInvokeStmt iStmt = (JInvokeStmt) callUnit;
//											InvokeExprBox ieb = (InvokeExprBox) iStmt.getInvokeExprBox();
//											Value v = ieb.getValue();
//											InstanceInvokeExpr jvie = (InstanceInvokeExpr) v;
//											base = (Local) jvie.getBase();
//										}
//										if(base != null){
//											res.add(new Abstraction(new EquivalentValue(base), source.getSource(), calleeMethod));
//										}
//									}
//									if (!calleeMethod.isStatic()) {
//										PointsToSet ptsGlobal = pta.reachingObjects(calleeMethod.getActiveBody().getThisLocal(), globalField);
//										if (ptsGlobal.hasNonEmptyIntersection(ptsSource)) {
//											Local base = null;
//											if(callUnit instanceof JAssignStmt){
//												base = (Local) ((InstanceInvokeExpr)((JAssignStmt)callUnit).getInvokeExpr()).getBase();
//											}
//											
//											if(callUnit instanceof JInvokeStmt){
//												JInvokeStmt iStmt = (JInvokeStmt) callUnit;
//												InvokeExprBox ieb = (InvokeExprBox) iStmt.getInvokeExprBox();
//												Value v = ieb.getValue();
//												InstanceInvokeExpr jvie = (InstanceInvokeExpr) v;
//												base = (Local) jvie.getBase();
//											}
//											if(base != null){
//												res.add(new Abstraction(new EquivalentValue(base), source.getSource(), calleeMethod));
//											}
//										}
//									}
//								}
//							}
							for (int i = 0; i < calleeMethod.getParameterCount(); i++) {
								if(calleeMethod.getActiveBody().getParameterLocal(i).equals(source.getTaintedObject().getValue())){
									if(callUnit instanceof Stmt){
										Stmt iStmt = (Stmt) callUnit;
										res.add(new Abstraction(new EquivalentValue(iStmt.getInvokeExpr().getArg(i)), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
									}
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
				if (call instanceof Stmt && ((Stmt) call).getInvokeExpr().getMethod().isNative()) {
					final Stmt iStmt = (Stmt) call;
					final List<Value> callArgs = iStmt.getInvokeExpr().getArgs();
					
					return new FlowFunction<Abstraction>() {

						public Set<Abstraction> computeTargets(Abstraction source) {

							if (callArgs.contains(source.getTaintedObject().getValue())) {
								// java uses call by value, but fields of complex objects can be changed (and tainted), so use this conservative approach:
								Set<Abstraction> res = new HashSet<Abstraction>();
								NativeCallHandler ncHandler = new DefaultNativeCallHandler();
								//TODO handle return value of native call
								res.addAll(ncHandler.getTaintedValues(iStmt, source, callArgs, interproceduralCFG().getMethodOf(unit)));
								
								return res;
							}
							return Collections.singleton(source);
						}
					};
				}
				if (call instanceof JAssignStmt) {
					final JAssignStmt stmt = (JAssignStmt) call;

					if (sourceManager.isSourceMethod(stmt.getInvokeExpr().getMethod())) {
						return new FlowFunction<Abstraction>() {

							@Override
							public Set<Abstraction> computeTargets(Abstraction source) {
								Set<Abstraction> res = new HashSet<Abstraction>();
								res.add(source);
								res.add(new Abstraction(new EquivalentValue(LocalBaseSelector.selectBase(stmt.getLeftOp())), new EquivalentValue(stmt.getInvokeExpr()), interproceduralCFG().getMethodOf(unit)));
								return res;
							}
						};
					}
				}
				return Identity.v();
			}
		};
	}

	public InfoflowLocalProblem(List<String> sourceList, List<String> sinks) {
		super(new JimpleBasedInterproceduralCFG());
		sourceManager = new DefaultSourceManager(sourceList);
		this.sinks = sinks;
	}

	public InfoflowLocalProblem(InterproceduralCFG<Unit, SootMethod> icfg, List<String> sourceList, List<String> sinks) {
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