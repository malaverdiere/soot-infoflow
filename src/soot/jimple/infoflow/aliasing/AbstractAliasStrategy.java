package soot.jimple.infoflow.aliasing;

import soot.jimple.infoflow.heros.IInfoflowCFG;
import soot.jimple.infoflow.heros.InfoflowSolver;

/**
 * Common base class for alias strategies
 * 
 * @author Steven Arzt
 */
public abstract class AbstractAliasStrategy implements IAliasingStrategy {

	private final IInfoflowCFG cfg;
	private InfoflowSolver fSolver;
	
	public AbstractAliasStrategy(IInfoflowCFG cfg) {
		this.cfg = cfg;
	}
	
	public IInfoflowCFG interproceduralCFG() {
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
