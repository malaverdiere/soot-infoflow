package soot.jimple.infoflow.data;

import soot.EquivalentValue;
import soot.SootMethod;

public class Abstraction {
	private final AccessPath accessPath;
	private final EquivalentValue source;
	private final SootMethod correspondingMethod;
	private int hashCode;
	

	public Abstraction(EquivalentValue taint, EquivalentValue src, SootMethod m){
		source = src;
		correspondingMethod = m;
		accessPath = new AccessPath(taint);
	}
	
	public Abstraction(AccessPath p, EquivalentValue src, SootMethod m){
		source = src;
		correspondingMethod = m;
		accessPath = p;
	}
	
	public EquivalentValue getSource() {
		return source;
	}
	
	
	public SootMethod getCorrespondingMethod() {
		return correspondingMethod;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
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
		} else if (source != other.source)
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		if(hashCode == 0){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			hashCode = result;
		}
		return hashCode;
	}
	
	@Override
	public String toString(){
		if(accessPath != null && source != null){
			return accessPath.toString() + " /source: "+ source.toString();
		}
		if(accessPath != null){
			return accessPath.toString();
		}
		return "Abstraction (null)";
	}
	
	public AccessPath getAccessPath(){
		return accessPath;
	}
	
}
