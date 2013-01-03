package soot.jimple.infoflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;

public abstract class AbstractInfoflowProblem extends DefaultJimpleIFDSTabulationProblem<Abstraction, InterproceduralCFG<Unit, SootMethod>> {

	final Set<Unit> initialSeeds = new HashSet<Unit>();
	final HashMap<String, List<String>> results;
	
	public AbstractInfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
		super(icfg);
		results = new HashMap<String, List<String>>();
	}

}
