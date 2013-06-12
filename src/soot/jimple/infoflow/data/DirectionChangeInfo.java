package soot.jimple.infoflow.data;

import soot.Unit;

public class DirectionChangeInfo {
	private Unit unitOfDirectionChange;
	private AccessPath accessPathOfDirectionChange;
	
	
	public Unit getUnitOfDirectionChange() {
		return unitOfDirectionChange;
	}
	public void setUnitOfDirectionChange(Unit unitOfDirectionChange) {
		this.unitOfDirectionChange = unitOfDirectionChange;
	}

	public AccessPath getAccessPathOfDirectionChange() {
		return accessPathOfDirectionChange;
	}
	public void setAccessPathOfDirectionChange(AccessPath accessPathOfDirectionChange) {
		this.accessPathOfDirectionChange = accessPathOfDirectionChange;
	}
	@Override
	public int hashCode() {
		return unitOfDirectionChange.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DirectionChangeInfo other = (DirectionChangeInfo) obj;
		if (accessPathOfDirectionChange == null) {
			if (other.accessPathOfDirectionChange != null)
				return false;
		} else if (!accessPathOfDirectionChange.equals(other.accessPathOfDirectionChange))
			return false;
		if (unitOfDirectionChange == null) {
			if (other.unitOfDirectionChange != null)
				return false;
		} else if (!unitOfDirectionChange.equals(other.unitOfDirectionChange))
			return false;
		return true;
	}
	
	public boolean isLoop(Unit u, Abstraction a){
		if(unitOfDirectionChange.equals(u)){
			if(accessPathOfDirectionChange.getPlainLocal() == null && a.getAccessPath().getPlainLocal() == null){
				return true;
			}
			if((accessPathOfDirectionChange.getPlainLocal() != null && a.getAccessPath().getPlainLocal() == null) || 
					(accessPathOfDirectionChange.getPlainLocal() == null && a.getAccessPath().getPlainLocal() != null))
				return false;
			if(accessPathOfDirectionChange.getPlainLocal().equals(a.getAccessPath().getPlainLocal())){
				return true;
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	public DirectionChangeInfo clone(){
		DirectionChangeInfo dci = new DirectionChangeInfo();
		dci.setAccessPathOfDirectionChange(accessPathOfDirectionChange);
		dci.setUnitOfDirectionChange(unitOfDirectionChange);
		return dci;
	}

}
