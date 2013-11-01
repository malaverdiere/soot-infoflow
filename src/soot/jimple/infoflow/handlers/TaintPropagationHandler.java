package soot.jimple.infoflow.handlers;

import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public interface TaintPropagationHandler {
	
	public enum FlowFunctionType {
		NormalFlowFunction,
		CallFlowFunction,
		CallToReturnFlowFunction,
		ReturnFlowFunction
	}

	public void notifyFlowIn
			(Unit stmt, Set<Abstraction> result,
			BiDiInterproceduralCFG<Unit, SootMethod> cfg,
			FlowFunctionType type);

	
}
