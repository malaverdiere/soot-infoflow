package soot.jimple.infoflow.data;

import soot.EquivalentValue;
import soot.SootMethod;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

public class Abstraction {
	private final AccessPath accessPath;
	private final EquivalentValue source;
	private final SootMethod correspondingMethod;
	

	public Abstraction(EquivalentValue taint, EquivalentValue src, SootMethod m){
		source = src;
		correspondingMethod = m;
		if(taint.getValue() instanceof StaticFieldRef){
			StaticFieldRef ref = (StaticFieldRef) taint.getValue();
			accessPath = new AccessPath(ref.getFieldRef().declaringClass().getName() + "."+ref.getFieldRef().name());
		} else if(taint.getValue() instanceof InstanceFieldRef){
			InstanceFieldRef ref = (InstanceFieldRef) taint.getValue();
			accessPath = new AccessPath(ref.getBase(), ref.getField().getName());
		}else{
			accessPath = new AccessPath(taint);
		}
		
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
		} else if (!source.equals(other.source))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
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
