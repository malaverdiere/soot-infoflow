package soot.jimple.interproc.ifds.problems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.NullType;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.interproc.ifds.FlowFunction;
import soot.jimple.interproc.ifds.FlowFunctions;
import soot.jimple.interproc.ifds.IFDSTabulationProblem;
import soot.jimple.interproc.ifds.InterproceduralCFG;
import soot.jimple.interproc.ifds.flowfunc.Identity;
import soot.jimple.interproc.ifds.flowfunc.KillAll;
import soot.jimple.interproc.ifds.template.JimpleBasedInterproceduralCFG;
import soot.toolkits.scalar.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

@SuppressWarnings("serial")
public class IFDSReachingDefinitions implements
		IFDSTabulationProblem<Unit, Pair<Value, Set<DefinitionStmt>>, SootMethod> {

	class ReachingDefinitionsFlowFunctions implements FlowFunctions<Unit, Pair<Value, Set<DefinitionStmt>>, SootMethod> {

		@Override
		public FlowFunction<Pair<Value, Set<DefinitionStmt>>> getNormalFlowFunction(final Unit curr, Unit succ) {
			if (curr instanceof DefinitionStmt) {
				final DefinitionStmt assignment = (DefinitionStmt) curr;

				return new FlowFunction<Pair<Value, Set<DefinitionStmt>>>() {
				
					@Override
					public Set<Pair<Value, Set<DefinitionStmt>>> computeTargets(Pair<Value, Set<DefinitionStmt>> source) {
						System.out.println("a " + source.getO1());
						for(DefinitionStmt x : source.getO2()){
							System.out.println("b " + x.getLeftOp() + " - " +  x.getRightOp());
						}
						
						if (source != zeroValue) {
							if (source.getO1().equivTo(assignment.getLeftOp())) {
								return Collections.emptySet();
							}
							return Collections.singleton(source);
						} else {
							return new HashSet<Pair<Value, Set<DefinitionStmt>>>() {
								{
									add(new Pair<Value, Set<DefinitionStmt>>(assignment.getLeftOp(),
											Collections.<DefinitionStmt> singleton(assignment)));
								}
							};
						}
					}
				};
			}

			return Identity.v();
		}

		@Override
		public FlowFunction<Pair<Value, Set<DefinitionStmt>>> getCallFlowFunction(Unit callStmt,
				SootMethod destinationMethod) {
			Stmt stmt = (Stmt) callStmt;
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			final List<Value> args = invokeExpr.getArgs();

			final List<Local> localArguments = new ArrayList<Local>(args.size());
			for (Value value : args) {
				if (value instanceof Local)
					localArguments.add((Local) value);
				else
					localArguments.add(null);
			}

			return new FlowFunction<Pair<Value, Set<DefinitionStmt>>>() {

				@Override
				public Set<Pair<Value, Set<DefinitionStmt>>> computeTargets(Pair<Value, Set<DefinitionStmt>> source) {
					for (Local localArgument : localArguments) {
						if (source.getO1().equivTo(localArgument)) {
							Pair<Value, Set<DefinitionStmt>> pair = new Pair<Value, Set<DefinitionStmt>>(Jimple.v()
									.newParameterRef(source.getO1().getType(), args.indexOf(localArgument)),
									source.getO2());
							return Collections.singleton(pair);
						}
					}

					return Collections.emptySet();
				}
			};
		}

		@Override
		public FlowFunction<Pair<Value, Set<DefinitionStmt>>> getReturnFlowFunction(final Unit callSite,
				SootMethod calleeMethod, final Unit exitStmt, Unit returnSite) {
			if (!(callSite instanceof DefinitionStmt))
				return KillAll.v();

			if (exitStmt instanceof ReturnVoidStmt)
				return KillAll.v();

			return new FlowFunction<Pair<Value, Set<DefinitionStmt>>>() {

				@Override
				public Set<Pair<Value, Set<DefinitionStmt>>> computeTargets(Pair<Value, Set<DefinitionStmt>> source) {
					if(exitStmt instanceof ReturnStmt) {
						ReturnStmt returnStmt = (ReturnStmt) exitStmt;
						if (returnStmt.getOp().equivTo(source.getO1())) {
							DefinitionStmt definitionStmt = (DefinitionStmt) callSite;
							Pair<Value, Set<DefinitionStmt>> pair = new Pair<Value, Set<DefinitionStmt>>(
									definitionStmt.getLeftOp(), source.getO2());
							return Collections.singleton(pair);
						}
					}
					return Collections.emptySet();
				}
			};
		}

		@Override
		public FlowFunction<Pair<Value, Set<DefinitionStmt>>> getCallToReturnFlowFunction(Unit callSite, Unit returnSite) {
			if (!(callSite instanceof DefinitionStmt))
				return Identity.v();
			
			final DefinitionStmt definitionStmt = (DefinitionStmt) callSite;
			return new FlowFunction<Pair<Value, Set<DefinitionStmt>>>() {

				@Override
				public Set<Pair<Value, Set<DefinitionStmt>>> computeTargets(Pair<Value, Set<DefinitionStmt>> source) {
					if(source.getO1().equivTo(definitionStmt.getLeftOp())) {
						return Collections.emptySet();
					} else {
						return Collections.singleton(source);
					}
				}
			};
		}

	}

	FlowFunctions<Unit, Pair<Value, Set<DefinitionStmt>>, SootMethod> flowFunctions = new ReachingDefinitionsFlowFunctions();

	Local zeroLocal = new JimpleLocal("<<zero>>", NullType.v());

	Pair<Value, Set<DefinitionStmt>> zeroValue = new Pair<Value, Set<DefinitionStmt>>(zeroLocal,
			Collections.<DefinitionStmt> emptySet());

	Multimap<SootMethod, Pair<Value, Set<DefinitionStmt>>> initialSeeds = HashMultimap.create();
	{
		initialSeeds.put(Scene.v().getMainMethod(), zeroValue);
	}

	@Override
	public FlowFunctions<Unit, Pair<Value, Set<DefinitionStmt>>, SootMethod> flowFunctions() {
		return flowFunctions;
	}

	@Override
	public InterproceduralCFG<Unit, SootMethod> interproceduralCFG() {
		return new JimpleBasedInterproceduralCFG();
	}

	@Override
	public Multimap<SootMethod, Pair<Value, Set<DefinitionStmt>>> initialSeeds() {
		return initialSeeds;
	}

	@Override
	public Pair<Value, Set<DefinitionStmt>> zeroValue() {
		return zeroValue;
	}

}
