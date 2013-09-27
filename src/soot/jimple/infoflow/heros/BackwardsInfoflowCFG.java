package soot.jimple.infoflow.heros;

import soot.Body;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.InverseGraph;

public class BackwardsInfoflowCFG extends InfoflowCFG {

	@Override
	protected DirectedGraph<Unit> makeGraph(Body body) {
		return new InverseGraph<Unit>(super.makeGraph(body));
	}
	
}
