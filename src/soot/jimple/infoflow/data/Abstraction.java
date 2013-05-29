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
	private boolean isActive = true;
	private final boolean exceptionThrown;
	private int hashCode;
	private Abstraction abstractionFromCallEdge;
	private Unit unitOfDirectionChange;

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
		this.accessPath = p;
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
		}
		else {
			source = original.source;
			sourceContext = original.sourceContext;
		}
		accessPath = p;
		exceptionThrown = original.exceptionThrown;
		activationUnit = original.activationUnit;
		isActive = original.isActive;
	}
	
	public Abstraction deriveInactiveAbstraction(){
		Abstraction a = clone();
		a.isActive = false;
		return a;
	}
	
	//should be only called by call-/returnFunctions!
	public Abstraction deriveNewAbstraction(AccessPath p){
		Abstraction a = new Abstraction(p, source, sourceContext, exceptionThrown, isActive);
		a.abstractionFromCallEdge = abstractionFromCallEdge;
		a.activationUnit = activationUnit;
		
		return a;
	}
	
	public Abstraction deriveNewAbstraction(AccessPath p, Unit newActUnit){
		Abstraction a = new Abstraction(p, source, sourceContext, exceptionThrown, isActive);
		a.abstractionFromCallEdge = abstractionFromCallEdge;
		if(isActive){
			a.activationUnit = newActUnit;
		}else{
			a.activationUnit = activationUnit;
		}
		return a;
	}
	
	public Abstraction deriveNewAbstraction(AccessPath p, Unit srcUnit, boolean isActive){
		Abstraction a = new Abstraction(p, source, sourceContext, exceptionThrown, isActive);
		a.abstractionFromCallEdge = abstractionFromCallEdge;
		if(isActive){
			a.activationUnit = srcUnit;
		}else{
			a.activationUnit = activationUnit;
		}
		return a;
	}
	
	public Abstraction deriveNewAbstraction(Value taint, Unit activationUnit){
		return this.deriveNewAbstraction(taint, false, activationUnit);
	}
	
	public Abstraction deriveNewAbstraction(Value taint, Unit srcUnit, boolean isActive){
		return this.deriveNewAbstraction(taint, false, srcUnit, isActive);
	}
	
	public Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField, Unit newActUnit){
		return deriveNewAbstraction(taint, cutFirstField, newActUnit, isActive);
	}
	
	public Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField, Unit newActUnit, boolean isActive){
		Abstraction a;
		if(cutFirstField){
			LinkedList<SootField> tempList = new LinkedList<SootField>(accessPath.getFields());
			tempList.removeFirst();
			a = new Abstraction(new AccessPath(taint, tempList), source, sourceContext, exceptionThrown, isActive);
		}
		else
			a = new Abstraction(new AccessPath(taint,accessPath.getFields()), source, sourceContext, exceptionThrown, isActive);
		a.abstractionFromCallEdge = abstractionFromCallEdge;
		if(isActive){
			a.activationUnit = newActUnit;
		}else{
			a.activationUnit = activationUnit;
		}
		return a;
	}

	/**
	 * Derives a new abstraction that models the current local being thrown as
	 * an exception
	 * @return The newly derived abstraction
	 */
	public Abstraction deriveNewAbstractionOnThrow(){
		assert !this.exceptionThrown;
		Abstraction abs = new Abstraction(accessPath, source, sourceContext, true, isActive);
		abs.abstractionFromCallEdge = abstractionFromCallEdge;
		return abs;
	}
	
	/**
	 * Derives a new abstraction that models the current local being caught as
	 * an exception
	 * @param taint The value in which the tainted exception is stored
	 * @return The newly derived abstraction
	 */
	public Abstraction deriveNewAbstractionOnCatch(Value taint, Unit newActivationUnit){
		assert this.exceptionThrown;
		Abstraction abs = new Abstraction(new AccessPath(taint), source, sourceContext, false, isActive);
		if(isActive){
			abs.activationUnit = newActivationUnit;
		}else{
			abs.activationUnit = activationUnit;
		}
		
		abs.abstractionFromCallEdge = abstractionFromCallEdge;
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
			return (isActive?"":"_")+accessPath.toString() + " | "+(activationUnit==null?"":activationUnit.toString());
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
		assert(activationUnit!=null);
		return activationUnit;
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
	public Abstraction clone(){
		Abstraction a = new Abstraction(accessPath, source, sourceContext, exceptionThrown, isActive);
		a.activationUnit = activationUnit;
		a.abstractionFromCallEdge = abstractionFromCallEdge;
		return a;
	}
	
	public Abstraction cloneUsePredAbstractionOfCG(){
		Abstraction a = clone();
		if(a.abstractionFromCallEdge != null){
			a.abstractionFromCallEdge = a.abstractionFromCallEdge.abstractionFromCallEdge;
		}
		return a;
	}

	public Unit getUnitOfDirectionChange() {
		return unitOfDirectionChange;
	}

	public void setUnitOfDirectionChange(Unit unitOfDirectionChange) {
		this.unitOfDirectionChange = unitOfDirectionChange;
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
		if (this.exceptionThrown != other.exceptionThrown)
			return false;
		if(this.isActive != other.isActive)
			return false;
		assert this.hashCode() == obj.hashCode();	// make sure nothing all wonky is going on
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
			this.hashCode = prime * this.hashCode + (exceptionThrown ? 1231 : 1237);
			this.hashCode = prime * this.hashCode + (isActive ? 1231 : 1237);
		}

		return this.hashCode;
	}

}
