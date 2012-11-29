package soot.jimple.infoflow;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.bodden.ide.FlowFunction;
import de.bodden.ide.FlowFunctions;
import de.bodden.ide.InterproceduralCFG;
import de.bodden.ide.flowfunc.Identity;

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
import soot.jimple.ReturnStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.source.DefaultSourceManager;
import soot.jimple.infoflow.source.SourceManager;
import soot.jimple.internal.InvokeExprBox;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JCastExpr;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.util.Chain;

public class InfoflowProblem extends DefaultJimpleIFDSTabulationProblem<Abstraction, InterproceduralCFG<Unit, SootMethod>> {

	final Set<Unit> initialSeeds = new HashSet<Unit>();
	final SourceManager sourceManager;
	final List<String> sinks;
	final HashMap<String, List<String>> results;
	final Abstraction zeroValue = new Abstraction(new JimpleLocal("zero", NullType.v()), null);

	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory() {
		return new FlowFunctions<Unit, Abstraction, SootMethod>() {

			public FlowFunction<Abstraction> getNormalFlowFunction(Unit src, Unit dest) {
				// if-Stmt -> take target and evaluate it.
				if (src instanceof soot.jimple.internal.JIfStmt) {
					soot.jimple.internal.JIfStmt ifStmt = (soot.jimple.internal.JIfStmt) src;
					src = ifStmt.getTarget();
				}
				
				// taint is propagated with assignStmt
				if (src instanceof AssignStmt) {
					AssignStmt assignStmt = (AssignStmt) src;
					Value right = assignStmt.getRightOp();
					Value left = assignStmt.getLeftOp();

					if (left instanceof ArrayRef) {
						left = ((ArrayRef) left).getBase();
					}

					final Value leftValue = left;
					final Value rightValue = right;
					final Unit srcUnit = src;

					return new FlowFunction<Abstraction>() {

						public Set<Abstraction> computeTargets(Abstraction source) {
							boolean addLeftValue = false;

								PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
								if (source.getTaintedObject() instanceof InstanceFieldRef && rightValue instanceof InstanceFieldRef) {
									InstanceFieldRef rightRef = (InstanceFieldRef) rightValue;
									InstanceFieldRef sourceRef = (InstanceFieldRef) source.getTaintedObject();

									if (rightRef.getField().getName().equals(sourceRef.getField().getName())) {
										Local rightBase = (Local) rightRef.getBase();
										
										
										PointsToSet ptsRight = pta.reachingObjects(rightBase);
										Local sourceBase = (Local) sourceRef.getBase();
										PointsToSet ptsSource2 = pta.reachingObjects(sourceBase);
										
										if (ptsRight.hasNonEmptyIntersection(ptsSource2)) {
											addLeftValue = true;
										}

									}
								}
								//special case:
								if(source.getTaintedObject().getType().toString().equals("java.lang.String")){
									if(rightValue instanceof InstanceFieldRef){
										InstanceFieldRef ref = (InstanceFieldRef) rightValue;
										if(source.getTaintedObject() instanceof Local){
											PointsToSet ptsSource = pta.reachingObjects((Local)source.getTaintedObject());
											PointsToSet ptsResult = pta.reachingObjects((Local)ref.getBase());
											if (ptsResult.hasNonEmptyIntersection(ptsSource)) {
												addLeftValue = true;
											}
										} //TODO: required for InstanceFieldRef, too?

									}
								}

								if (rightValue instanceof InstanceFieldRef && source.getTaintedObject() instanceof Local && !source.equals(zeroValue)) {
									PointsToSet ptsSource = pta.reachingObjects((Local) source.getTaintedObject());
									InstanceFieldRef rightRef = (InstanceFieldRef) rightValue;
									PointsToSet ptsResult = pta.reachingObjects(ptsSource, rightRef.getField());

									if (!ptsResult.isEmpty()) {
										addLeftValue = true;
									}

								}

								if (rightValue instanceof StaticFieldRef && source.getTaintedObject() instanceof StaticFieldRef) {
									StaticFieldRef rightRef = (StaticFieldRef) rightValue;
									StaticFieldRef sourceRef = (StaticFieldRef) source.getTaintedObject();
									if (rightRef.getFieldRef().name().equals(sourceRef.getFieldRef().name()) && rightRef.getFieldRef().declaringClass().equals(sourceRef.getFieldRef().declaringClass())) {
										addLeftValue = true;
									}
								}

								if (rightValue instanceof ArrayRef) {
									Local rightBase = (Local) ((ArrayRef) rightValue).getBase();
									if (source.getTaintedObject().equals(rightBase) || (source.getTaintedObject() instanceof Local && pta.reachingObjects(rightBase).hasNonEmptyIntersection(pta.reachingObjects((Local) source.getTaintedObject())))) {
										addLeftValue = true;
									}
								}
								if (rightValue instanceof JCastExpr) {
									if (source.getTaintedObject().equals(((JCastExpr) rightValue).getOpBox().getValue())) {
										addLeftValue = true;
									}
								}

								// generic case, is true for Locals, ArrayRefs that are equal etc..
								if (source.getTaintedObject().equals(rightValue)) {
									addLeftValue = true;
								}
								// also check if there are two arrays (or anything else?) that have the same origin
								if (source.getTaintedObject() instanceof Local && rightValue instanceof Local) {
									PointsToSet ptsRight = pta.reachingObjects((Local) rightValue);
									PointsToSet ptsSource = pta.reachingObjects((Local) source.getTaintedObject());
									if (ptsRight.hasNonEmptyIntersection(ptsSource)) {
//										addLeftValue = true;//bei concreteArrayListNegativeTest: untainted hasnonemptyintersection with tainted Oo 
									}
								}

								// if one of them is true -> add leftValue
								if (addLeftValue) {
									Set<Abstraction> res = new HashSet<Abstraction>();
									//performance improvement: do not insert this -
									source.addToAlias(leftValue);
									res.add(source);
									res.add(new Abstraction(leftValue, source.getSource(), interproceduralCFG().getMethodOf(srcUnit)));
									
									//// TODO: go recursively through the definitions before, compute pts
									SootMethod m = interproceduralCFG().getMethodOf(srcUnit);
									Chain<Local> localChain = m.getActiveBody().getLocals();
									PointsToSet ptsLeft;
									if(leftValue instanceof Local){
										ptsLeft = pta.reachingObjects((Local) leftValue);
									
										for(Local c :localChain){
											PointsToSet ptsVal = pta.reachingObjects(c);
											if(ptsLeft.hasNonEmptyIntersection(ptsVal) && !leftValue.equals(c)){
//												res.add(new Abstraction(c, source.getSource(), interproceduralCFG().getMethodOf(srcUnit)));
											}
										}
									} else if(leftValue instanceof InstanceFieldRef){
										for(Local c :localChain){
											PointsToSet pts = pta.reachingObjects(c, ((InstanceFieldRef)leftValue).getField());
											if(!pts.isEmpty()){
//												res.add(new Abstraction(c, source.getSource(), interproceduralCFG().getMethodOf(srcUnit)));
											}
										}
									}
									
									return res;
							}
							//TODO: for delete insert whole magic from above again:
							if (leftValue instanceof InstanceInvokeExpr) {
								if (source.getTaintedObject() instanceof InstanceFieldRef && leftValue instanceof InstanceFieldRef) {
									InstanceFieldRef leftRef = (InstanceFieldRef) leftValue;
									InstanceFieldRef sourceRef = (InstanceFieldRef) source.getTaintedObject();

									if (leftRef.getField().getName().equals(sourceRef.getField().getName())) {
										Local leftBase = (Local) leftRef.getBase();
										
										
										PointsToSet ptsLeft = pta.reachingObjects(leftBase);
										Local sourceBase = (Local) sourceRef.getBase();
										PointsToSet ptsSource2 = pta.reachingObjects(sourceBase);
										
										if (ptsLeft.hasNonEmptyIntersection(ptsSource2)) {
											return Collections.emptySet(); 
										}

									}
								}
							}
							if (source.getTaintedObject().equals(leftValue)) {
								return Collections.emptySet(); 
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
						PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
						Value base = null;
						Set<Abstraction> res = new HashSet<Abstraction>();
						List<Value> taintedParams = new LinkedList<Value>();
						//if taintedobject is  instancefieldRef we have to check if the object is delivered..
						if(source.getTaintedObject() instanceof InstanceFieldRef && ie instanceof InstanceInvokeExpr){
							
							//second, they might be changed as param - check this
							InstanceFieldRef ref = (InstanceFieldRef) source.getTaintedObject();
							base = ref.getBase();
							//first, instancefieldRefs must be propagated if they come from the same class:
							InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
															
							
									
							PointsToSet ptsSource = pta.reachingObjects((Local)base);
							PointsToSet ptsCall = pta.reachingObjects((Local)vie.getBase());
							if (ptsCall.hasNonEmptyIntersection(ptsSource)) {
								res.add(source); 
							}						
					
						}
												
						//check if whole object is tainted (happens with strings, for example:)
						if(!dest.isStatic() && ie instanceof InstanceInvokeExpr && source.getTaintedObject() instanceof Local){
							InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
							PointsToSet ptsSelf = pta.reachingObjects((Local) vie.getBase());
							PointsToSet ptsSource = pta.reachingObjects((Local)source.getTaintedObject());
							if (ptsSelf.hasNonEmptyIntersection(ptsSource)) {
								res.add(new Abstraction(dest.getActiveBody().getThisLocal(), source.getSource(), dest));
								
								
							}
						}
						
						//check if param is tainted:
							for (int i = 0; i < callArgs.size(); i++) {
								if (callArgs.get(i).equals(source.getTaintedObject())) {
									Abstraction abs = new Abstraction(paramLocals.get(i), source.getSource(), dest);
									abs.addToAlias(callArgs.get(i));
									res.add(abs);
									taintedParams.add(paramLocals.get(i));
								}
								//if base != null we have an instanceFieldRef
								if(base != null && callArgs.get(i).equals(base)){
									InstanceFieldRef ref = (InstanceFieldRef) source.getTaintedObject();
									InstanceFieldRef testRef = (InstanceFieldRef) ref.clone();
									testRef.setBase(paramLocals.get(i));
									Abstraction abs = new Abstraction(testRef, source.getSource(), dest);
									abs.addToAlias(source.getTaintedObject());
									taintedParams.add(paramLocals.get(i));
									res.add(abs);
								}
							}
						
						
							//if we have called a sink we have to store the path from the source - in case one of the params is tainted!
							if (sinks.contains(dest.toString())) {
								if(!taintedParams.isEmpty()){
									if (!results.containsKey(dest.toString())) {
										List<String> list = new ArrayList<String>();
										list.add(source.getSource().toString());
										results.put(dest.toString(), list);
									} else {
										results.get(dest.toString()).add(source.getSource().toString());
									}
								}
							}
						
						//TODO: erweitere results sodass auch objekte, deren fields tainted sind, ausgegeben werden!
						//fieldRefs must be analyzed even if they are not part of the params:
						if (source.getTaintedObject() instanceof StaticFieldRef) {
							res.add(source); 	
						}
						//zu allgemein, hier müsste wrapping/unwrapping greifen?
						if(source.getTaintedObject() instanceof InstanceFieldRef){
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
						Set<Abstraction> res = new HashSet<Abstraction>();
						
						//if we have a returnStmt we have to look at the returned value:
						if (exitUnit instanceof ReturnStmt) {
							ReturnStmt returnStmt = (ReturnStmt) exitUnit;
							Value op = returnStmt.getOp();
							if (op instanceof Value) {
								if (callUnit instanceof DefinitionStmt) {
									DefinitionStmt defnStmt = (DefinitionStmt) callUnit;
									Value leftOp = defnStmt.getLeftOp();
									final Value retLocal = op;
									
									if (source.getTaintedObject().equals(retLocal)) {
										res.add(new Abstraction(leftOp, source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
									}
									if(source.getTaintedObject() instanceof InstanceFieldRef){
										InstanceFieldRef ifr = (InstanceFieldRef) source.getTaintedObject();
										if(ifr.getBase().equals(retLocal)){
											InstanceFieldRef newRef = (InstanceFieldRef)ifr.clone();
											newRef.setBase(leftOp);
											//if we have a type change we have to adjust the field ??
											
											
											res.add(new Abstraction(newRef, source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
										}
										
									}
									
									
								}
			
							}
						}
						if(callUnit instanceof JInvokeStmt && !source.equals(zeroValue)){
							JInvokeStmt jiStmt = (JInvokeStmt) callUnit;
							if(jiStmt.getInvokeExpr() instanceof InstanceInvokeExpr){
								InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) jiStmt.getInvokeExpr();
								PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
								PointsToSet ptsThis = pta.reachingObjects((Local) iIExpr.getBase());
								if(source.getTaintedObject() instanceof Local){
									PointsToSet ptsSource = pta.reachingObjects((Local) source.getTaintedObject());
									if(ptsSource.hasNonEmptyIntersection(ptsThis)){
//										res.add(new Abstraction( iIExpr.getBase(), source.getTaintedObject(), interproceduralCFG().getMethodOf(callUnit) ));
									}
								}else if(source.getTaintedObject() instanceof InstanceFieldRef){
									PointsToSet ptsSource = pta.reachingObjects((Local) ((InstanceFieldRef) source.getTaintedObject()).getBase());
									if(ptsSource.hasNonEmptyIntersection(ptsThis)){
										InstanceFieldRef newRef = (InstanceFieldRef) ((InstanceFieldRef) source.getTaintedObject()).clone();
										newRef.setBase(iIExpr.getBase());
//										res.add(new Abstraction(newRef, source.getTaintedObject(), interproceduralCFG().getMethodOf(callUnit)));
									}
								}
								
								
								
							}
						}
						
						//easy: static
						if (source.getTaintedObject() instanceof StaticFieldRef) {
							res.add(source);
						}
						
						if(source.getTaintedObject() instanceof InstanceFieldRef){
							//check if base is param, if true exchange with alias-set
							InstanceFieldRef ifr = (InstanceFieldRef) source.getTaintedObject();
							Value base = ifr.getBase();
							//TODO: rekursiv gestalten?
							boolean found = false;
							Value originalBase = null;
							for (int i = 0; i < calleeMethod.getParameterCount(); i++) {
								if(calleeMethod.getActiveBody().getParameterLocal(i).equals(base)){ //or pts?
									found = true;
									if(callUnit instanceof Stmt){
										Stmt iStmt = (Stmt) callUnit;
										originalBase = iStmt.getInvokeExpr().getArg(i);
										
									}
								//evtl. nicht genug, es kann auch einfach auf dem Objekt aufgerufen worden sein!
								}
							}
							if(originalBase != null && found){
								for(Value val :source.getAliasSet()){
									if(val instanceof InstanceFieldRef){ //!val.equals(source.getTaintedObject() -> can be solved without alias set in easy case..
										InstanceFieldRef newRef = (InstanceFieldRef)val.clone();
										
										newRef.setBase(originalBase);//TODO: does this work?
										res.add(new Abstraction(newRef, source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
									}
								}
							}
							//aktuelles local ermitteln (am Besten am Anfang...)
							Local thisL = null;
							if(!calleeMethod.isStatic()){
								thisL =calleeMethod.getActiveBody().getThisLocal();
							}
							if(thisL != null){
								if(!ifr.getBase().equals(thisL)){
									//check if it is not one of the params (then we have already fixed it)
									boolean param = false;
									for (int i = 0; i < calleeMethod.getParameterCount(); i++) {
										if(calleeMethod.getActiveBody().getParameterLocal(i).equals(base)){
											param = true;
										}
									}
									if(!param){
										res.add(new Abstraction(source.getTaintedObject(), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
									}
									
								}else{
									//there is only one case in which this must be added, too: if the caller-Method has the same thisLocal - check this:
									//for super-calls we have to use pts
									PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
									PointsToSet ptsSource = pta.reachingObjects((Local) ifr.getBase());
									PointsToSet ptsThis = pta.reachingObjects(thisL);
									
									if(ptsSource.hasNonEmptyIntersection(ptsThis) || ifr.getBase().equals(thisL)){
										boolean param = false;
										//check if it is not one of the params (then we have already fixed it)
										for (int i = 0; i < calleeMethod.getParameterCount(); i++) {
											if(calleeMethod.getActiveBody().getParameterLocal(i).equals(base)){
												param = true;
											}
										}
										if(!param){
											InstanceFieldRef newRef = (InstanceFieldRef) ifr.clone();
											newRef.setBase(thisL);
											
											res.add(new Abstraction(newRef, source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
										}
										
									}
									
	
								}
							}
						}
						
						if (source.getTaintedObject() instanceof Local) {
							//reassign the ones we changed into local params:
							PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
							PointsToSet ptsRight = pta.reachingObjects((Local) source.getTaintedObject());

							for (SootField globalField : calleeMethod.getDeclaringClass().getFields()) {
								if (globalField.isStatic()) {
									PointsToSet ptsGlobal = pta.reachingObjects(globalField);
									if (ptsRight.hasNonEmptyIntersection(ptsGlobal)) {
										res.add(new Abstraction(Jimple.v().newStaticFieldRef(globalField.makeRef()), source.getSource()));
									}
								} else {
									//might not work through several levels - base is always replaced - should be only replaced if
									// this is the current base?
									if (!calleeMethod.isStatic()) {
										PointsToSet ptsGlobal = pta.reachingObjects(calleeMethod.getActiveBody().getThisLocal(), globalField);
										if (ptsGlobal.hasNonEmptyIntersection(ptsRight)) {
											Local thisL = null;
											if(callUnit instanceof JAssignStmt){
												thisL = (Local) ((InstanceInvokeExpr)((JAssignStmt)callUnit).getInvokeExpr()).getBase();
											}
											
											if(callUnit instanceof JInvokeStmt){
												JInvokeStmt iStmt = (JInvokeStmt) callUnit;
												InvokeExprBox ieb = (InvokeExprBox) iStmt.getInvokeExprBox();
												Value v = ieb.getValue();
												InstanceInvokeExpr jvie = (InstanceInvokeExpr) v;
												thisL = (Local) jvie.getBase();
											}
											if(thisL != null){
												SootFieldRef ref = globalField.makeRef();
												InstanceFieldRef fRef = Jimple.v().newInstanceFieldRef(thisL, ref);
												res.add(new Abstraction(fRef, source.getSource(), calleeMethod));
											}
										}
									}
								}
							}
							for (int i = 0; i < calleeMethod.getParameterCount(); i++) {
								if(calleeMethod.getActiveBody().getParameterLocal(i).equals(source.getTaintedObject())){ //or pts?
									if(callUnit instanceof Stmt){
										Stmt iStmt = (Stmt) callUnit;
										res.add(new Abstraction(iStmt.getInvokeExpr().getArg(i), source.getSource(), interproceduralCFG().getMethodOf(callUnit)));
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
					return new FlowFunction<Abstraction>() {

						public Set<Abstraction> computeTargets(Abstraction source) {

							if (callArgs.contains(source.getTaintedObject())) {
								// "res.add(new Pair<Value, Value>(paramLocals.get(argIndex), source.getO2()));" is not enough:
								// java uses call by value, but fields of complex objects can be changed (and tainted), so use this conservative approach:
								Set<Abstraction> res = new HashSet<Abstraction>();
								for (int i = 0; i < paramLocals.size(); i++) {
									res.add(new Abstraction(paramLocals.get(i), source.getSource(), interproceduralCFG().getMethodOf(unit)));
								}
								return res;
							}
							return Collections.emptySet();
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
								res.add(new Abstraction(stmt.getLeftOp(), stmt.getInvokeExpr(), interproceduralCFG().getMethodOf(unit)));
								return res;
							}
						};
					}
				}
				return Identity.v();
			}
		};
	}

	public InfoflowProblem(List<String> sourceList, List<String> sinks) {
		super(new JimpleBasedInterproceduralCFG());
		sourceManager = new DefaultSourceManager(sourceList);
		this.sinks = sinks;
		results = new HashMap<String, List<String>>();
	}

	public InfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg, List<String> sourceList, List<String> sinks) {
		super(icfg);
		sourceManager = new DefaultSourceManager(sourceList);
		this.sinks = sinks;
		results = new HashMap<String, List<String>>();
	}

	public Abstraction createZeroValue() {
		if (zeroValue == null)
			return new Abstraction(new JimpleLocal("zero", NullType.v()), null);

		return zeroValue;
	}

	@Override
	public Set<Unit> initialSeeds() {
		return initialSeeds;

	}
}