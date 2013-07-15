package soot.jimple.infoflow.handlers;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.InfoflowResults;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

/**
 * Handler that is called when information flow results become available
 * @author Steven Arzt
 */
public interface ResultsAvailableHandler {

	/**
	 * Callback that is invoked when information flow results are available
	 * @param cfg The program graph
	 * @param results The results that were computed
	 */
	public void onResultsAvailable(BiDiInterproceduralCFG<Unit, SootMethod> cfg,
			InfoflowResults results);

}
