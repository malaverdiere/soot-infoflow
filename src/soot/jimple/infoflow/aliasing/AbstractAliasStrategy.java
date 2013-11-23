package soot.jimple.infoflow.aliasing;

import soot.jimple.infoflow.heros.InfoflowCFG;
import soot.jimple.infoflow.heros.InfoflowSolver;

/**
 * Common base class for alias strategies
 * 
 * @author Steven Arzt
 */
public abstract class AbstractAliasStrategy implements IAliasingStrategy {

	private final InfoflowCFG cfg;
	private InfoflowSolver fSolver;
	
	public AbstractAliasStrategy(InfoflowCFG cfg) {
		this.cfg = cfg;
	}
	
	public InfoflowCFG interproceduralCFG() {
		return this.cfg;
	}
	
	@Override
	public void setForwardSolver(InfoflowSolver fSolver) {
		this.fSolver = fSolver;
	}
	
	protected InfoflowSolver getForwardSolver() {
		return this.fSolver;
	}

}
