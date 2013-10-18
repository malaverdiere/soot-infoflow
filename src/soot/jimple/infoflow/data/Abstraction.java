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


import heros.solver.Pair;

import java.util.Collections;
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
	}
	
	public class SourceContextAndPath extends SourceContext {
		private final List<Stmt> path = new LinkedList<Stmt>();
		
		public SourceContextAndPath(Value value, Stmt stmt) {
			super(value, stmt);
			path.add(stmt);
		}
		
		public List<Stmt> getPath() {
			return this.path;
		}
		
		@Override
		public boolean equals(Object other) {
			if (other == null || !(other instanceof SourceContextAndPath))
				return false;
			if (!super.equals(other))
				return false;
			SourceContextAndPath scap = (SourceContextAndPath) other;
			return this.path.equals(scap.path);
		}
		
		@Override
		public int hashCode() {
			return 31 * super.hashCode() + 7 * path.hashCode();
		}
		
	}
	
	/**
	 * the access path contains the currently tainted variable or field
	 */
	private final AccessPath accessPath;
	
	private final Set<Pair<Abstraction, Stmt>> predecessors = new HashSet<Pair<Abstraction, Stmt>>();
	private SourceContext sourceContext = null;
	
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
		this.sourceContext = new SourceContext(currentVal, currentStmt);
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
			sourceContext = null;
			exceptionThrown = false;
			activationUnit = null;
			conditionalCallSite = null;
		}
		else {
			sourceContext = original.sourceContext;
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
		abs.predecessors.add(new Pair<Abstraction, Stmt>(this, currentStmt));
		assert abs.predecessors.size() == 1;
		abs.sourceContext = null;
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
	 * @param throwStmt The statement at which the exception was thrown
	 * @return The newly derived abstraction
	 */
	public final Abstraction deriveNewAbstractionOnThrow(Stmt throwStmt){
		assert !this.exceptionThrown;
		Abstraction abs = cloneWithPredecessor(throwStmt);
		abs.sourceContext = null;
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
		
	/**
	 * Gets the path of statements from the source to the current statement
	 * with which this abstraction is associated. If this path is ambiguous,
	 * a single path is selected randomly.
	 * @return The path from the source to the current statement
	 */
	public Set<SourceContextAndPath> getPaths() {
		return getPath(Collections.<Pair<Abstraction, Stmt>>emptySet());
	}
		
	private Set<SourceContextAndPath> getPath(Set<Pair<Abstraction, Stmt>> doneSet) {
		Set<SourceContextAndPath> pathList = new HashSet<SourceContextAndPath>();

		// If this statement has a context, we create a path context from it
		if (sourceContext != null) {
			SourceContextAndPath sourceAndPath = new SourceContextAndPath
					(sourceContext.value, sourceContext.stmt);
			pathList.add(sourceAndPath);
			return pathList;
		}

		// Look for predecessors to extend a path
		for (Pair<Abstraction, Stmt> prevEntry : predecessors) {
			// On every path we explore, we also track the abstractions to
			// make sure that we don't run in circles
			Set<Pair<Abstraction, Stmt>> prevDoneSet = new HashSet<Pair<Abstraction, Stmt>>();
			prevDoneSet.addAll(doneSet);
			if (!prevDoneSet.add(prevEntry))
				continue;

			// See if there is a valid path from this predecessor up to a source
			Set<SourceContextAndPath> prevPath = prevEntry.getO1().getPath(prevDoneSet);
			if (prevPath.isEmpty())
				continue;
			if (!pathList.addAll(prevPath))
				continue;

			// Extend the paths we have found with the current statement
			if (!prevEntry.getO1().isAbstractionActive())
				if (prevEntry.getO2() != null)
					for (SourceContextAndPath context : prevPath)
						context.getPath().add(prevEntry.getO2());
		}
		
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
		Abstraction a = cloneWithPredecessor((Stmt) this.activationUnit);
		a.sourceContext = null;
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
		abs.sourceContext = null;
		abs.conditionalCallSite = null;
		return abs;
	}

	public final Abstraction dropTopPostdominator() {
		if (postdominators.isEmpty())
			return this;
		
		Abstraction abs = clone();
		abs.sourceContext = null;
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
		return cloneWithPredecessor(null);
	}
	
	public Abstraction cloneWithPredecessor(Stmt predStmt) {
		Abstraction abs = new Abstraction(accessPath, this);
		if (predStmt == null)
			abs.predecessors.add(new Pair<Abstraction, Stmt>(this, null));
		else
			abs.predecessors.add(new Pair<Abstraction, Stmt>(this, predStmt));
		assert abs.predecessors.size() == 1;
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
		if (sourceContext == null) {
			if (other.sourceContext != null)
				return false;
		} else if (!sourceContext.equals(other.sourceContext))
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
			this.hashCode = prime * this.hashCode + ((sourceContext == null) ? 0 : sourceContext.hashCode());
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
	 * Gets the context of the taint, i.e. the statement and value of the source
	 * @return The statement and value of the source
	 */
	public SourceContext getSourceContext() {
		return sourceContext;
	}

	/**
	 * Merges the predecessors of the current abstraction with those of the
	 * given one
	 * @param abs The abstraction with which to merge predecessors
	 */
	public void mergePredecessors(Abstraction abs) {
		assert this.equals(abs);
		
		if (abs != this)
			mergePredecessors(abs, Collections.<Pair<Abstraction, Stmt>>emptySet());
	}

	private void mergePredecessors(Abstraction abs, Set<Pair<Abstraction, Stmt>> doneSet) {
		for (Pair<Abstraction, Stmt> absVal : abs.predecessors) {
			if (!predecessors.contains(absVal)) {
				predecessors.add(absVal);
				continue;
			}
				
			Pair<Abstraction, Stmt> curVal = null;	
			for (Pair<Abstraction, Stmt> tmp : predecessors)
				if (tmp.equals(absVal)) {
					curVal = tmp;
					break;
				}
			assert curVal != null;
			
			// If this very abstraction is already in the list, we can skip it
			if (curVal == absVal)
				continue;
			
			// Make sure not to add cyclic predecessors
			if (doneSet.contains(absVal))
				continue;

			Set<Pair<Abstraction, Stmt>> prevDoneSet = new HashSet<Pair<Abstraction, Stmt>>();
			prevDoneSet.addAll(doneSet);
			prevDoneSet.add(absVal);
			curVal.getO1().mergePredecessors(absVal.getO1(), prevDoneSet);
		}

		/*
		for (Pair<Abstraction, Stmt> absVal : abs.predecessors)
			if (absVal.getO1() != this)
				this.predecessors.add(absVal);
		*/
	}
	
}
