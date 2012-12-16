package soot.jimple.infoflow.data;

import java.util.HashSet;
import java.util.Set;

import soot.EquivalentValue;
import soot.SootMethod;
import soot.Value;
import soot.jimple.internal.JInstanceFieldRef;

public class Abstraction {
	private final EquivalentValue taintedObject;
	private final EquivalentValue source;
	private final Set<Value> aliasSet;
	private final SootMethod correspondingMethod;
	

	public Abstraction(EquivalentValue taint, EquivalentValue src, SootMethod m){
		aliasSet = new HashSet<Value>();
		taintedObject = taint;
		source = src;
		aliasSet.add(taint);
		correspondingMethod = m;
	}
	
	public Abstraction(EquivalentValue taint, EquivalentValue src, SootMethod m, Set<Value> aliases){
		aliasSet = aliases;
		taintedObject = taint;
		source = src;
		correspondingMethod = m;
	}
	
	public EquivalentValue getTaintedObject() {
		return taintedObject;
	}
	
	public EquivalentValue getSource() {
		return source;
	}
	
	public Set<Value> getAliasSet() {
		return aliasSet;
	}
	
	@Deprecated
	public void addToAlias(Value val){
		boolean found = false;
		if(val instanceof JInstanceFieldRef){
			for(Value current : aliasSet){
				if(current instanceof JInstanceFieldRef){
					JInstanceFieldRef r1 = (JInstanceFieldRef) current;
					JInstanceFieldRef r2 = (JInstanceFieldRef) val;
					if(r1.getBase().equals(r2.getBase()) &&
							r1.getField().equals(r2.getField())){
						found = true;
					}
					
				}
			}
			
		}
		if(!found){
			aliasSet.add(val);
		}
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
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (taintedObject == null) {
			if (other.taintedObject != null)
				return false;
		} else if (!taintedObject.equals(other.taintedObject))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((taintedObject == null) ? 0 : taintedObject.hashCode());
		return result;
	}
	
	@Override
	public String toString(){
		if(taintedObject != null && source != null){
			return taintedObject.toString() + "("+taintedObject.getType().toString()+ ") /source: "+ source.toString();
		}
		if(taintedObject != null){
			return taintedObject.toString()+ "("+taintedObject.getType().toString() +")";
		}
		return "Abstraction (null)";
	}
	

}
