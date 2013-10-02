/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow.data;

import heros.InterproceduralCFG;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
/**
 * subclass of abstraction which additionally stores the complete path between source and sink
 *
 */
public class AbstractionWithPath extends Abstraction {
	private final List<Unit> propagationPath;

	public AbstractionWithPath(Value taint, Value src, Stmt srcContext,
			boolean exceptionThrown, boolean isActive, Unit activationUnit){
		super(taint, src, srcContext, exceptionThrown, isActive, activationUnit);
		propagationPath = new ArrayList<Unit>();
	}
	

	protected AbstractionWithPath(Value taint, AbstractionWithPath src){
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

	@Override
	public AbstractionWithPath deriveNewAbstraction(AccessPath p){
		return new AbstractionWithPath(p, this);
	}
	
	@Override
	public Abstraction clone(){
		AbstractionWithPath a = new AbstractionWithPath(getAccessPath(), this);
		assert this.equals(a);
		return a;
	}

}
