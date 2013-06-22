package soot.jimple.infoflow.data;


import java.util.LinkedList;

import soot.SootField;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

public class Abstraction implements Cloneable {
	private final AccessPath accessPath;
	private final Value source;
	private final Stmt sourceContext;
	private Unit activationUnit;
	private Unit activationUnitOnCurrentLevel;
	private boolean isActive;
	private boolean exceptionThrown;
	private int hashCode;
	private Abstraction abstractionFromCallEdge;

	public Abstraction(Value taint, Value src, Stmt srcContext, boolean exceptionThrown, boolean isActive, Unit activationUnit){
		this.source = src;
		this.accessPath = new AccessPath(taint);
		this.activationUnit = activationUnit;
		this.sourceContext = srcContext;
		this.exceptionThrown = exceptionThrown;
		this.isActive = isActive;
	}
		
	protected Abstraction(AccessPath p, Value src, Stmt srcContext, boolean exceptionThrown, boolean isActive){
		this.source = src;
		this.sourceContext = srcContext;
		this.accessPath = p.clone();
		this.activationUnit = null;
		this.exceptionThrown = exceptionThrown;
		this.isActive = isActive;
	}

	/**
	 * Creates an abstraction as a copy of an existing abstraction,
	 * only exchanging the access path.
	 * @param p The value to be used as the new access path
	 * @param original The original abstraction to copy
	 */
	public Abstraction(Value p, Abstraction original){
		this(new AccessPath(p), original);
	}

	/**
	 * Creates an abstraction as a copy of an existing abstraction,
	 * only exchanging the access path. -> only used by AbstractionWithPath
	 * @param p The access path for the new abstraction
	 * @param original The original abstraction to copy
	 */
	public Abstraction(AccessPath p, Abstraction original){
		if (original == null) {
			source = null;
			sourceContext = null;
			exceptionThrown = false;
			activationUnit = null;
			activationUnitOnCurrentLevel = null;
			abstractionFromCallEdge = null;
		}
		else {
			source = original.source;
			sourceContext = original.sourceContext;
			exceptionThrown = original.exceptionThrown;
			activationUnit = original.activationUnit;
			activationUnitOnCurrentLevel = original.activationUnitOnCurrentLevel;
			abstractionFromCallEdge = original.abstractionFromCallEdge;
			isActive = original.isActive;
		}
		accessPath = p.clone();
	}
	
	public final Abstraction deriveInactiveAbstraction(){
		Abstraction a = clone();
		a.isActive = false;
		return a;
	}
	
	//should be only called by call-/returnFunctions!
	public Abstraction deriveNewAbstraction(AccessPath p){
		return new Abstraction(p, this);
	}
	
	public final Abstraction deriveNewAbstraction(AccessPath p, Unit newActUnit){
		Abstraction a = deriveNewAbstraction(p);
		if(isActive)
			a.activationUnit = newActUnit;
		return a;
	}
	
	public final Abstraction deriveNewAbstraction(AccessPath p, Unit srcUnit, boolean isActive){
		Abstraction a = deriveNewAbstraction(p);
		a.isActive = isActive;
		if (isActive)
			a.activationUnit = srcUnit;
		return a;
	}
	
	public final Abstraction deriveNewAbstraction(Value taint, Unit activationUnit){
		return this.deriveNewAbstraction(taint, false, activationUnit);
	}
	
	public final Abstraction deriveNewAbstraction(Value taint, Unit srcUnit, boolean isActive){
		return this.deriveNewAbstraction(taint, false, srcUnit, isActive);
	}
	
	public final Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField, Unit newActUnit){
		return deriveNewAbstraction(taint, cutFirstField, newActUnit, isActive);
	}
	
	public final Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField, Unit newActUnit, boolean isActive){
		Abstraction a;
		LinkedList<SootField> tempList = new LinkedList<SootField>(accessPath.getFields());
		if(cutFirstField){
			tempList.removeFirst();
		}
		a = deriveNewAbstraction(new AccessPath(taint, tempList));
		a.isActive = isActive;
		if (isActive)
			a.activationUnit = newActUnit;
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
		Abstraction abs = deriveNewAbstraction(new AccessPath(taint));
		abs.exceptionThrown = false;
		if(isActive)
			abs.activationUnit = newActivationUnit;
		return abs;
	}

	public Value getSource() {
		return source;
	}

	public Stmt getSourceContext() {
		return this.sourceContext;
	}
	
	public boolean isAbstractionActive(){
		return isActive;
	}
	
	@Override
	public String toString(){
		if(accessPath != null && source != null){
			return (isActive?"":"_")+accessPath.toString() + " | "+(activationUnit==null?"":activationUnit.toString()) + ">>"+ (activationUnitOnCurrentLevel==null?"":activationUnitOnCurrentLevel.toString());
		}
		if(accessPath != null){
			return accessPath.toString();
		}
		return "Abstraction (null)";
	}
	
	public AccessPath getAccessPath(){
		return accessPath;
	}
	
	public Unit getActivationUnit(){
		return activationUnit;
	}
	
	public Abstraction getAbstractionWithNewActivationUnitOnCurrentLevel(Unit u){
		Abstraction a = this.clone();
		a.activationUnitOnCurrentLevel = u;
		return a;
	}
	
	public Unit getActivationUnitOnCurrentLevel(){
		return activationUnitOnCurrentLevel;
	}
	
	public Abstraction getAbstractionFromCallEdge(){
		return abstractionFromCallEdge;
	}
	/**
	 * best-effort approach: if we are at level 0 and have not seen a call edge, we just take the abstraction which is imprecise
	 * null is not allowed
	 * @return
	 */
	public Abstraction getNotNullAbstractionFromCallEdge(){
		if(abstractionFromCallEdge == null)
			return this;
		/*
		Abstraction abs = abstractionFromCallEdge.clone();
		abs.abstractionFromCallEdge = abs.clone();
		return abs;
		*/
		return abstractionFromCallEdge;
	}
	
	public void usePredAbstractionOfCG(){
		if(abstractionFromCallEdge == null)
			return;
		abstractionFromCallEdge = abstractionFromCallEdge.abstractionFromCallEdge;
		
	}
	
	public void setAbstractionFromCallEdge(Abstraction abs){
		abstractionFromCallEdge = abs;
	}
	
	public Abstraction getActiveCopy(boolean dropAbstractionFromCallEdge){
		Abstraction a = clone();
		a.isActive = true;
		// do not kill the original activation point since we might return into
		// a caller, find a new alias there and then need to know where both
		// aliases originally became active.
//		a.activationUnit = null;
		a.activationUnitOnCurrentLevel = null;
		if(dropAbstractionFromCallEdge){
			if(a.abstractionFromCallEdge != null){
				a.abstractionFromCallEdge = a.abstractionFromCallEdge.abstractionFromCallEdge;
			}
		}
		
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
	
	@Override
	public Abstraction clone() {
		return new Abstraction(accessPath, this);
	}
	
	public Abstraction cloneUsePredAbstractionOfCG(){
		Abstraction a = clone();
		if(a.abstractionFromCallEdge != null){
			a.abstractionFromCallEdge = a.abstractionFromCallEdge.abstractionFromCallEdge;
		}
		return a;
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
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
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
		if (activationUnitOnCurrentLevel == null) {
			if (other.activationUnitOnCurrentLevel != null)
				return false;
		} else if (!activationUnitOnCurrentLevel.equals(other.activationUnitOnCurrentLevel))
			return false;
		if (this.exceptionThrown != other.exceptionThrown)
			return false;
		if(this.isActive != other.isActive)
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		if (this.hashCode == 0) {
			this.hashCode = 1;
			this.hashCode = prime * this.hashCode + ((accessPath == null) ? 0 : accessPath.hashCode());
			this.hashCode = prime * this.hashCode + ((source == null) ? 0 : source.hashCode());
			this.hashCode = prime * this.hashCode + ((sourceContext == null) ? 0 : sourceContext.hashCode());
			this.hashCode = prime * this.hashCode + ((activationUnit == null) ? 0 : activationUnit.hashCode());
			this.hashCode = prime * this.hashCode + ((activationUnitOnCurrentLevel == null) ? 0 : activationUnitOnCurrentLevel.hashCode());
			this.hashCode = prime * this.hashCode + (exceptionThrown ? 1231 : 1237);
			this.hashCode = prime * this.hashCode + (isActive ? 1231 : 1237);
		}
		return hashCode;
	}
		
}
