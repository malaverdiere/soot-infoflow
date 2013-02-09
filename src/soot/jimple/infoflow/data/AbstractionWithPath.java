package soot.jimple.infoflow.data;

import heros.InterproceduralCFG;

import java.util.ArrayList;
import java.util.List;

import soot.EquivalentValue;
import soot.SootMethod;
import soot.Unit;

public class AbstractionWithPath extends Abstraction {
	private final List<Unit> propagationPath;
	

	public AbstractionWithPath(EquivalentValue taint, EquivalentValue src, SootMethod m, boolean fieldsTainted){
		super(taint, src, m, fieldsTainted);
		propagationPath = new ArrayList<Unit>();
	}
	
	public AbstractionWithPath(EquivalentValue taint, EquivalentValue src, SootMethod m, List<Unit> path, boolean fieldsTainted){
		super(taint, src, m, fieldsTainted);
		propagationPath = new ArrayList<Unit>(path);
	}

	public AbstractionWithPath(EquivalentValue taint, EquivalentValue src, SootMethod m, List<Unit> path, Unit s, boolean fieldsTainted){
		this(taint, src, m, path, fieldsTainted);
		propagationPath.add(s);
	}

	public AbstractionWithPath(AccessPath p, EquivalentValue src, SootMethod m){
		super(p, src, m);
		propagationPath = new ArrayList<Unit>();		
	}
	
	public AbstractionWithPath(AccessPath p, EquivalentValue src, SootMethod m, List<Unit> path){
		super(p, src, m);
		propagationPath = new ArrayList<Unit>(path);
	}

	public AbstractionWithPath(AccessPath p, EquivalentValue src, SootMethod m, List<Unit> path, Unit s){
		this(p, src, m, path);
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
