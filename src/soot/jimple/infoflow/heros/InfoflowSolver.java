package soot.jimple.infoflow.heros;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import heros.solver.CountingThreadPoolExecutor;
import heros.solver.PathEdge;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;

public class InfoflowSolver extends JimpleIFDSSolver<Abstraction, InterproceduralCFG<Unit, SootMethod>> {

	public InfoflowSolver(IFDSTabulationProblem<Unit, Abstraction, SootMethod, InterproceduralCFG<Unit, SootMethod>> problem, boolean dumpResults, CountingThreadPoolExecutor executor) {
		super(problem, dumpResults);
		this.executor = executor;
	}
	
	@Override
	protected CountingThreadPoolExecutor getExecutor() {
		return executor;
	}

	public void processEdge(PathEdge<Unit, Abstraction, SootMethod> edge){
		scheduleEdgeProcessing(edge);
	}
}
