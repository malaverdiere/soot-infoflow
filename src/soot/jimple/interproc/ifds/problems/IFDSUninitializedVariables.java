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
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.interproc.ifds.FlowFunction;
import soot.jimple.interproc.ifds.FlowFunctions;
import soot.jimple.interproc.ifds.IFDSTabulationProblem;
import soot.jimple.interproc.ifds.InterproceduralCFG;
import soot.jimple.interproc.ifds.flowfunc.Identity;
import soot.jimple.interproc.ifds.flowfunc.Kill;
import soot.jimple.interproc.ifds.flowfunc.KillAll;
import soot.jimple.interproc.ifds.template.JimpleBasedInterproceduralCFG;
import soot.util.Chain;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class IFDSUninitializedVariables implements IFDSTabulationProblem<Unit, Value, SootMethod> {

	class UninitializedVariablesFlowFunctions implements FlowFunctions<Unit, Value, SootMethod> {

		@Override
		public FlowFunction<Value> getNormalFlowFunction(Unit curr, Unit succ) {
			if (curr instanceof DefinitionStmt) {
				final DefinitionStmt definition = (DefinitionStmt) curr;
				final Value leftOp = definition.getLeftOp();

				return new FlowFunction<Value>() {

					@Override
					public Set<Value> computeTargets(final Value source) {
						List<ValueBox> useBoxes = definition.getUseBoxes();
						for (ValueBox valueBox : useBoxes) {
							if (valueBox.getValue().equivTo(source))
								return new HashSet<Value>() { { add(source); add(leftOp); } };
						}

						if (leftOp.equivTo(source))
							return Collections.emptySet();

						return Collections.singleton(source);
					}

				};
			}

			return Identity.v();
		}

		@Override
		public FlowFunction<Value> getCallFlowFunction(Unit callStmt, final SootMethod destinationMethod) {
			Stmt stmt = (Stmt) callStmt;
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			final List<Value> args = invokeExpr.getArgs();

			final List<Local> localArguments = new ArrayList<Local>();
			for (Value value : args)
				if (value instanceof Local)
					localArguments.add((Local) value);

			return new FlowFunction<Value>() {

				@Override
				public Set<Value> computeTargets(final Value source) {
					for (Local localArgument : localArguments) {
						if (source.equivTo(localArgument)) {
							return Collections.<Value> singleton(Jimple.v().newParameterRef(source.getType(),
									args.indexOf(localArgument)));
						}
					}

					if (source == zeroValue) {
						final Chain<Local> locals = destinationMethod.getActiveBody().getLocals();
						return new HashSet<Value>() { { addAll(locals); } };
					}

					return Collections.emptySet();
				}

			};
		}

		@Override
		public FlowFunction<Value> getReturnFlowFunction(final Unit callSite, SootMethod calleeMethod,
				final Unit exitStmt, Unit returnSite) {
			if (callSite instanceof DefinitionStmt) {
				final DefinitionStmt definition = (DefinitionStmt) callSite;
				if (exitStmt instanceof ReturnStmt) {
					return new FlowFunction<Value>() {

						@Override
						public Set<Value> computeTargets(Value source) {
							ReturnStmt returnStmt = (ReturnStmt) exitStmt;
							if (returnStmt.getOp().equivTo(source))
								return Collections.singleton(definition.getLeftOp());
							return Collections.emptySet();
						}

					};
				} else if (exitStmt instanceof ThrowStmt) {
					return new FlowFunction<Value>() {

						@Override
						public Set<Value> computeTargets(final Value source) {
							if (source == zeroValue)
								return new HashSet<Value>() { { add(definition.getLeftOp()); } };
							else
								return Collections.emptySet();
						}
						
					};
				}
			}
			
			return KillAll.v();
		}

		@Override
		public FlowFunction<Value> getCallToReturnFlowFunction(Unit callSite, Unit returnSite) {
			if (callSite instanceof DefinitionStmt) {
				DefinitionStmt definition = (DefinitionStmt) callSite;

				return new Kill<Value>(definition.getLeftOp());
			}
			return Identity.v();
		}
	}

	protected Value zeroValue = new JimpleLocal("<<zero>>", NullType.v());

	protected FlowFunctions<Unit, Value, SootMethod> flowFunctions = new UninitializedVariablesFlowFunctions();

	private Multimap<SootMethod, Value> initialSeeds = HashMultimap.create();
	{
		SootMethod mainMethod = Scene.v().getMainMethod();
		initialSeeds.putAll(mainMethod, mainMethod.getActiveBody().getLocals());
	}

	@Override
	public FlowFunctions<Unit, Value, SootMethod> flowFunctions() {
		return flowFunctions;
	}

	@Override
	public InterproceduralCFG<Unit, SootMethod> interproceduralCFG() {
		return new JimpleBasedInterproceduralCFG();
	}

	@Override
	public Multimap<SootMethod, Value> initialSeeds() {
		return initialSeeds;
	}

	@Override
	public Value zeroValue() {
		return zeroValue;
	}

}
