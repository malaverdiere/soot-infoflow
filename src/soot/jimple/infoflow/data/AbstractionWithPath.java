package soot.jimple.infoflow.data;

import heros.InterproceduralCFG;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

public class AbstractionWithPath extends Abstraction {
	private final List<Unit> propagationPath;
	


	public AbstractionWithPath(Value taint, Value src, boolean fieldsTainted, Stmt srcContext){
		super(taint, src, fieldsTainted, srcContext);
		propagationPath = new ArrayList<Unit>();
	}
	

	public AbstractionWithPath(Value taint, AbstractionWithPath src, boolean fieldsTainted){
		super(taint, src, fieldsTainted);
		if (src == null)
			propagationPath = new ArrayList<Unit>();
		else
			propagationPath = new ArrayList<Unit>(src.propagationPath);
	}

	public AbstractionWithPath(AccessPath p, AbstractionWithPath src){
		super(p, src);
		if (src == null)
			propagationPath = new ArrayList<Unit>();
		else
			propagationPath = new ArrayList<Unit>(src.getPropagationPath());		
	}

	public AbstractionWithPath(AccessPath p, Value src, Stmt srcContext){
		super(p, src, srcContext);
		propagationPath = new ArrayList<Unit>();		
	}
	
	public AbstractionWithPath(AccessPath p, Value src, Stmt srcContext, List<Unit> path){
		super(p, src, srcContext);
		propagationPath = new ArrayList<Unit>(path);
	}

	public AbstractionWithPath(AccessPath p, Value src, List<Unit> path, Unit s){
		this(p, src,(Stmt)s, path);
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
	
	/**
	 * Adds an element to the propagation path and return this very object for
	 * convenience 
	 * @param element The element to add to the propagation path
	 * @return This object
	 */
	public AbstractionWithPath addPathElement(Unit element) {
		this.propagationPath.add(element);
		return this;
	}

}
