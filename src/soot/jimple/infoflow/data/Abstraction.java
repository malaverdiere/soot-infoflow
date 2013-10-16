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


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.infoflow.heros.InfoflowCFG.UnitContainer;
/**
 * the abstraction class contains all information that is necessary to track the taint.
 *
 */
public class Abstraction implements Cloneable {

	/**
	 * Class representing a source value together with the statement that created it
	 * 
	 * @author Steven Arzt
	 */
	public class SourceContext {
		private final Value value;
		private final Stmt stmt;
		
		public SourceContext(Value value, Stmt stmt) {
			this.value = value;
			this.stmt = stmt;
		}
		
		public Value getValue() {
			return this.value;
		}
		
		public Stmt getStmt() {
			return this.stmt;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || !(obj instanceof SourceContext))
				return false;
			SourceContext other = (SourceContext) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (stmt == null) {
				if (other.stmt != null)
					return false;
			} else if (!stmt.equals(other.stmt))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		private Abstraction getOuterType() {
			return Abstraction.this;
		}
	}
	
	/**
	 * the access path contains the currently tainted variable or field
	 */
	private final AccessPath accessPath;
	
	private final Set<Abstraction> predecessors = new HashSet<Abstraction>();
	private Value currentVal = null;
	private Stmt currentStmt = null;
	
	/**
	 * Unit/Stmt which activates the taint when the abstraction passes it
	 */
	private Unit activationUnit;
	/**
	 * active abstraction is tainted value,
	 * inactive abstraction is an alias to a tainted value that
	 * might be activated in the future
	 */
	private boolean isActive;
	/**
	 * taint is thrown by an exception (is set to false when it reaches the catch-Stmt)
	 */
	private boolean exceptionThrown;
	private int hashCode;

	/**
	 * The postdominators we need to pass in order to leave the current conditional
	 * branch
	 */
	private final Stack<UnitContainer> postdominators = new Stack<UnitContainer>();
	/**
	 * The conditional call site. If this site is set, the current code is running
	 * inside a conditionally-called method.
	 */
	private Unit conditionalCallSite = null;

	public Abstraction(Value taint, Value currentVal, Stmt currentStmt,
			boolean exceptionThrown, boolean isActive, Unit activationUnit){
		this.currentVal = currentVal;
		this.currentStmt = currentStmt;

		this.accessPath = new AccessPath(taint);
		this.activationUnit = activationUnit;
		this.exceptionThrown = exceptionThrown;
		this.isActive = isActive;
	}

	/**
	 * Creates an abstraction as a copy of an existing abstraction,
	 * only exchanging the access path. -> only used by AbstractionWithPath
	 * @param p The access path for the new abstraction
	 * @param original The original abstraction to copy
	 */
	protected Abstraction(AccessPath p, Abstraction original){
		if (original == null) {
			currentVal = null;
			currentStmt = null;
			
			exceptionThrown = false;
			activationUnit = null;
			conditionalCallSite = null;
		}
		else {
			currentVal = original.currentVal;
			currentStmt = original.currentStmt;
			
			exceptionThrown = original.exceptionThrown;
			activationUnit = original.activationUnit;
			isActive = original.isActive;
			postdominators.addAll(original.postdominators);
			assert this.postdominators.equals(original.postdominators);
			conditionalCallSite = original.conditionalCallSite;
		}
		accessPath = p;
	}
	
	public final Abstraction deriveInactiveAbstraction(){
		return deriveInactiveAbstraction(accessPath);
	}
	
	public final Abstraction deriveInactiveAbstraction(AccessPath p){
		Abstraction a = deriveNewAbstraction(p, null);
		a.isActive = false;
		return a;
	}

	public Abstraction deriveNewAbstraction(AccessPath p, Stmt currentStmt){
		Abstraction abs = new Abstraction(p, this);
		abs.predecessors.add(this);
		abs.currentVal = null;
		abs.currentStmt = currentStmt;
		return abs;
	}
			
	public final Abstraction deriveNewAbstraction(Value taint, Unit activationUnit){
		return this.deriveNewAbstraction(taint, false, activationUnit);
	}
	
	public final Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField, Unit newActUnit){
		Abstraction a;
		SootField[] orgFields = accessPath.getFields();
		SootField[] fields = new SootField[cutFirstField ? orgFields.length - 1 : orgFields.length];
		for (int i = cutFirstField ? 1 : 0; i < orgFields.length; i++)
			fields[cutFirstField ? i - 1 : i] = orgFields[i];
		a = deriveNewAbstraction(new AccessPath(taint, fields), (Stmt) newActUnit);
		if (isActive) {
			assert newActUnit != null;
			a.activationUnit = newActUnit;
		}
		return a;
	}

	/**
	 * Derives a new abstraction that models the current local being thrown as
	 * an exception
	 * @return The newly derived abstraction
	 */
	public final Abstraction deriveNewAbstractionOnThrow(){
		assert !this.exceptionThrown;
		Abstraction abs = clone();
		abs.currentVal = null;
		abs.currentStmt = null;
		abs.exceptionThrown = true;
		return abs;
	}
	
	/**
	 * Derives a new abstraction that models the current local being caught as
	 * an exception
	 * @param taint The value in which the tainted exception is stored
	 * @return The newly derived abstraction
	 */
	public final Abstraction deriveNewAbstractionOnCatch(Value taint, Unit newActivationUnit){
		assert this.exceptionThrown;
		Abstraction abs = deriveNewAbstraction(new AccessPath(taint), (Stmt) newActivationUnit);
		abs.exceptionThrown = false;
		if(isActive) {
			assert newActivationUnit != null;
			abs.activationUnit = newActivationUnit;
		}
		return abs;
	}
	
	private Set<Abstraction> getRootAbstractions() {
		List<Abstraction> workList = new LinkedList<Abstraction>();
		Set<Abstraction> doneSet = new HashSet<Abstraction>();
		Set<Abstraction> rootAbstractions = new HashSet<Abstraction>();
		workList.add(this);
		
		while (!workList.isEmpty()) {
			Abstraction curAbs = workList.remove(0);
			
			// Schedule further predecessors
			for (Abstraction prev : curAbs.predecessors)
				if (doneSet.add(prev))
					workList.add(prev);
			
			// Evaluate tree roots
			if (curAbs.predecessors.isEmpty())
				rootAbstractions.add(curAbs);
		}
		
		return rootAbstractions;		
	}
	

	public Set<SourceContext> getSources() {
		Set<SourceContext> rootSources = new HashSet<SourceContext>();
		for (Abstraction abs : getRootAbstractions())
			rootSources.add(new SourceContext(abs.currentVal, abs.currentStmt));
		return rootSources;
	}
	
	/**
	 * Gets the path of statements from the source to the current statement
	 * with which this abstraction is associated. If this path is ambiguous,
	 * a single path is selected randomly.
	 * @return The path from the source to the current statement
	 */
	public List<Stmt> getPath() {
		// Get a path from a random predecessor
		List<Stmt> pathList = null;
		for (Abstraction prevAbs : predecessors) {
			pathList = prevAbs.getPath();
			break;
		}
		
		// If we are at the source, we have to generate the list
		if (pathList == null)
			pathList = new LinkedList<Stmt>();
		
		// Add the current statement
		if (currentStmt != null)
			pathList.add(currentStmt);
		
		return pathList;
	}
	
	public boolean isAbstractionActive(){
		return isActive;
	}
	
	@Override
	public String toString(){
		return (isActive?"":"_")+accessPath.toString() + " | "+(activationUnit==null?"":activationUnit.toString()) + ">>";
	}
	
	public AccessPath getAccessPath(){
		return accessPath;
	}
	
	public Unit getActivationUnit(){
		return activationUnit;
	}
	
	public Abstraction getActiveCopy(){
		Abstraction a = clone();
		a.currentVal = null;
		a.currentStmt = null;
		a.isActive = true;
		// do not kill the original activation point since we might return into
		// a caller, find a new alias there and then need to know where both
		// aliases originally became active.
//		a.activationUnit = null;
//		a.activationUnitOnCurrentLevel.clear();
		return a;
	}
	
	/**
	 * Gets whether this value has been thrown as an exception
	 * @return True if this value has been thrown as an exception, otherwise
	 * false
	 */
	public boolean getExceptionThrown() {
		return this.exceptionThrown;
	}
	
	public final Abstraction deriveConditionalAbstractionEnter(UnitContainer postdom,
			Stmt conditionalUnit) {
		if (!postdominators.isEmpty() && postdominators.contains(postdom))
			return this;
		assert this.conditionalCallSite == null;
		
		Abstraction abs = deriveNewAbstraction(AccessPath.getEmptyAccessPath(), conditionalUnit);
		abs.postdominators.push(postdom);
		return abs;
	}
	
	public final Abstraction deriveConditionalAbstractionCall(Unit conditionalCallSite) {
		assert conditionalCallSite != null;
		
		Abstraction abs = deriveNewAbstraction(AccessPath.getEmptyAccessPath(), (Stmt) conditionalCallSite);
		abs.conditionalCallSite = conditionalCallSite;
		abs.isActive = true;

//		abs.activationUnit = conditionalCallSite;
//		abs.activationUnitOnCurrentLevel.clear();
		return abs;
	}
	
	public final Abstraction leaveConditionalCall() {
		Abstraction abs = clone();
		abs.currentVal = null;
		abs.currentStmt = null;
		abs.conditionalCallSite = null;
		return abs;
	}

	public final Abstraction dropTopPostdominator() {
		if (postdominators.isEmpty())
			return this;
		
		Abstraction abs = clone();
		abs.currentVal = null;
		abs.currentStmt = null;
		abs.postdominators.pop();
		return abs;
	}
	
	public UnitContainer getTopPostdominator() {
		if (this.postdominators.isEmpty())
			return null;
		return this.postdominators.peek();
	}
	
	public boolean isTopPostdominator(Unit u) {
		UnitContainer uc = getTopPostdominator();
		if (uc == null)
			return false;
		return uc.getUnit() == u;
	}

	public boolean isTopPostdominator(SootMethod sm) {
		UnitContainer uc = getTopPostdominator();
		if (uc == null)
			return false;
		return uc.getMethod() == sm;
	}
	
	public Unit getConditionalCallSite() {
		return this.conditionalCallSite;
	}

	@Override
	public Abstraction clone() {
		Abstraction abs = new Abstraction(accessPath, this);
		abs.predecessors.add(this);
		assert abs.equals(this);
		return abs;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj))
			return true;
		if (obj == null || !(obj instanceof Abstraction))
			return false;
		Abstraction other = (Abstraction) obj;
		if (accessPath == null) {
			if (other.accessPath != null)
				return false;
		} else if (!accessPath.equals(other.accessPath))
			return false;
		
		return localEquals(other);
	}
	
	/**
	 * Checks whether this object locally equals the given object, i.e. the both
	 * are equal modulo the access path
	 * @param other The object to compare this object with
	 * @return True if this object is locally equal to the given one, otherwise
	 * false
	 */
	private boolean localEquals(Abstraction other) {
		// deliberately ignore prevAbs
		if (currentVal == null) {
			if (other.currentVal != null)
				return false;
		} else if (!currentVal.equals(other.currentVal))
			return false;
		if (currentStmt == null) {
			if (other.currentStmt != null)
				return false;
		} else if (!currentStmt.equals(other.currentStmt))
			return false;

		if (activationUnit == null) {
			if (other.activationUnit != null)
				return false;
		} else if (!activationUnit.equals(other.activationUnit))
			return false;
		if (this.exceptionThrown != other.exceptionThrown)
			return false;
		if(this.isActive != other.isActive)
			return false;
		if(!this.postdominators.equals(other.postdominators))
			return false;
		if (conditionalCallSite == null) {
			if (other.conditionalCallSite != null)
				return false;
		} else if (!conditionalCallSite.equals(other.conditionalCallSite))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		if (this.hashCode == 0) {
			this.hashCode = 1;

			// deliberately ignore prevAbs
			this.hashCode = prime * this.hashCode + ((currentVal == null) ? 0 : currentVal.hashCode());
			this.hashCode = prime * this.hashCode + ((currentStmt == null) ? 0 : currentStmt.hashCode());

			this.hashCode = prime * this.hashCode + ((accessPath == null) ? 0 : accessPath.hashCode());
			this.hashCode = prime * this.hashCode + ((activationUnit == null) ? 0 : activationUnit.hashCode());
			this.hashCode = prime * this.hashCode + (exceptionThrown ? 1231 : 1237);
			this.hashCode = prime * this.hashCode + (isActive ? 1231 : 1237);
			this.hashCode = prime * this.hashCode + postdominators.hashCode();
			this.hashCode = prime * this.hashCode + ((conditionalCallSite == null) ? 0 : conditionalCallSite.hashCode());
		}
		return hashCode;
	}
	
	/**
	 * Checks whether this abstraction entails the given abstraction, i.e. this
	 * taint also taints everything that is tainted by the given taint.
	 * @param other The other taint abstraction
	 * @return True if this object at least taints everything that is also tainted
	 * by the given object
	 */
	public boolean entails(Abstraction other) {
		if (accessPath == null) {
			if (other.accessPath != null)
				return false;
		} else if (!accessPath.entails(other.accessPath))
			return false;
		return localEquals(other);
	}

	/**
	 * Gets the value associated with this abstraction. For abstractions at
	 * sources, this is usually the expression that created the taint.
	 * @return The value associated with this abstraction
	 */
	public Value getCurrentVal() {
		return currentVal;
	}

	/**
	 * Gets the statement associated with this abstraction.
	 * @return The statement associated with this abstraction
	 */
	public Stmt getCurrentStmt() {
		return currentStmt;
	}
	
	/**
	 * Merges the predecessors of the current abstraction with those of the
	 * given one
	 * @param abs The abstraction with which to merge predecessors
	 */
	public void mergePredecessors(Abstraction abs) {
		for (Abstraction pred : abs.predecessors)
			if (pred != this)
				predecessors.add(pred);
	}

}
