package soot.jimple.infoflow.data;

import heros.InterproceduralCFG;

import java.util.ArrayList;
import java.util.List;

import soot.EquivalentValue;
import soot.SootMethod;
import soot.Unit;

public class AbstractionWithPath extends Abstraction {
	private final List<Unit> propagationPath;
	

	public AbstractionWithPath(EquivalentValue taint, EquivalentValue src){
		super(taint, src);
		propagationPath = new ArrayList<Unit>();
	}
	
	public AbstractionWithPath(EquivalentValue taint, EquivalentValue src, List<Unit> path){
		super(taint, src);
		if (path == null)
			propagationPath = new ArrayList<Unit>();
		else
			propagationPath = new ArrayList<Unit>(path);
	}

	public AbstractionWithPath(EquivalentValue taint, EquivalentValue src, List<Unit> path, Unit s){
		this(taint, src, path);
		propagationPath.add(s);
	}

	public AbstractionWithPath(AccessPath p, EquivalentValue src){
		super(p, src);
		propagationPath = new ArrayList<Unit>();		
	}
	
	public AbstractionWithPath(AccessPath p, EquivalentValue src, List<Unit> path){
		super(p, src);
		propagationPath = new ArrayList<Unit>(path);
	}

	public AbstractionWithPath(AccessPath p, EquivalentValue src, List<Unit> path, Unit s){
		this(p, src, path);
		if (s != null)
			propagationPath.add(s);
	}
	
	public List<Unit> getPropagationPath() {
		return this.propagationPath;
	}

	public List<String> getPropagationPathAsString(InterproceduralCFG<Unit, SootMethod> cfg) {
		List<String> res = new ArrayList<String>();
		for (Unit u : this.propagationPath)
			res.add(cfg.getMethodOf(u) + ": " + u.toString());
		return res;
	}

}
