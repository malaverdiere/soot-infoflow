package soot.jimple.infoflow.heros;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import soot.jimple.toolkits.ide.icfg.BackwardsInterproceduralCFG;
import heros.IFDSTabulationProblem;
import heros.solver.CountingThreadPoolExecutor;

public class BackwardSolver extends JimpleIFDSSolver<Abstraction, BackwardsInterproceduralCFG> {
	private IFDSTabulationProblem<Unit, Abstraction, SootMethod, BackwardsInterproceduralCFG> problem;
	
	
	public BackwardSolver(IFDSTabulationProblem<Unit, Abstraction, SootMethod, BackwardsInterproceduralCFG> problem, boolean dumpResults, CountingThreadPoolExecutor executor) {
		super(problem, dumpResults);
		this.executor = executor;
		this.problem = problem;
	}

	@Override
	protected CountingThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	public IFDSTabulationProblem<Unit, Abstraction, SootMethod, BackwardsInterproceduralCFG> getProblem(){
		return problem;
	}
	
}
