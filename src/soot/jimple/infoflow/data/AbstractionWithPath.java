package soot.jimple.infoflow.data;

import heros.InterproceduralCFG;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

public class AbstractionWithPath extends Abstraction {
	private final List<Unit> propagationPath;

	public AbstractionWithPath(Value taint, Value src, Stmt srcContext, boolean exceptionThrown, boolean isActive, Unit activationUnit){
		super(taint, src, srcContext, exceptionThrown, isActive, activationUnit);
		propagationPath = new ArrayList<Unit>();
	}
	

	public AbstractionWithPath(Value taint, AbstractionWithPath src){
		super(taint, src);
		if (src == null)
			propagationPath = new ArrayList<Unit>();
		else
			propagationPath = new ArrayList<Unit>(src.propagationPath);
	}

	protected AbstractionWithPath(AccessPath p, AbstractionWithPath src){
		super(p, src);
		if (src == null)
			propagationPath = new ArrayList<Unit>();
		else
			propagationPath = new ArrayList<Unit>(src.getPropagationPath());		
	}
	
	public AbstractionWithPath(AccessPath p, Value src, Stmt srcContext, boolean exceptionThrown, boolean isActive, List<Unit> path){
		super(p, src, srcContext, exceptionThrown, isActive);
		propagationPath = new ArrayList<Unit>(path);
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

	public AbstractionWithPath deriveNewAbstraction(AccessPath p){
		AbstractionWithPath a = new AbstractionWithPath(p, this.getSource(),
				this.getSourceContext(), this.getExceptionThrown(), this.isAbstractionActive(), this.propagationPath);
		return a;
	}
	
	public AbstractionWithPath deriveNewAbstraction(Value taint, Unit activationUnit){
		return this.deriveNewAbstraction(taint, false, activationUnit);
	}
	
	@Override
	public AbstractionWithPath deriveNewAbstraction(Value taint, boolean cutFirstField, Unit activationUnit){ //TODO:
		AbstractionWithPath a;
		if(cutFirstField){
			LinkedList<SootField> tempList = new LinkedList<SootField>(this.getAccessPath().getFields());
			tempList.removeFirst();
			a = new AbstractionWithPath(new AccessPath(taint, tempList), this.getSource(),
					this.getSourceContext(), this.getExceptionThrown(), this.isAbstractionActive(), this.propagationPath);
		}
		else
			a = new AbstractionWithPath(new AccessPath(taint, this.getAccessPath().getFields()),
					this.getSource(), this.getSourceContext(), this.getExceptionThrown(), this.isAbstractionActive(), this.propagationPath);
		return a;
	}

	/**
	 * Derives a new abstraction that models the current local being thrown as
	 * an exception
	 * @return The newly derived abstraction
	 */
	@Override
	public AbstractionWithPath deriveNewAbstractionOnThrow(){
		assert this.getAccessPath().isLocal();
		assert !this.getExceptionThrown();
		AbstractionWithPath abs = new AbstractionWithPath(this.getAccessPath(), this.getSource(),
				this.getSourceContext(), true, this.isAbstractionActive(), this.propagationPath);
		return abs;
	}

	/**
	 * Derives a new abstraction that models the current local being caught as
	 * an exception
	 * @param taint The value in which the tainted exception is stored
	 * @return The newly derived abstraction
	 */
	@Override
	public AbstractionWithPath deriveNewAbstractionOnCatch(Value taint, Unit actUnit){ //TODO
		assert this.getAccessPath().isLocal();
		assert !this.getExceptionThrown();
		AbstractionWithPath abs = new AbstractionWithPath(new AccessPath(taint), this.getSource(),
				this.getSourceContext(), false, this.isAbstractionActive(), this.propagationPath);
		return abs;
	}

}
