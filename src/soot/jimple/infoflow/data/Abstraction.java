package soot.jimple.infoflow.data;

import java.util.HashSet;
import java.util.Set;

import soot.SootMethod;
import soot.Value;
import soot.jimple.internal.JInstanceFieldRef;

public class Abstraction {
	private Value taintedObject;
	private Value source;
	private Set<Value> aliasSet;
	private SootMethod correspondingMethod;
	
	public Abstraction(){
		aliasSet = new HashSet<Value>();
	}
	
	public Abstraction(Value taint, Value src){
		aliasSet = new HashSet<Value>();
		taintedObject = taint;
		source = src;
		aliasSet.add(taint);
		
	}
	
	public Abstraction(Value taint, Value src, SootMethod m){
		aliasSet = new HashSet<Value>();
		taintedObject = taint;
		source = src;
		aliasSet.add(taint);
		correspondingMethod = m;
	}
	
	public Value getTaintedObject() {
		return taintedObject;
	}
	public void setTaintedObject(Value taintedObject) {
		this.taintedObject = taintedObject;
	}
	public Value getSource() {
		return source;
	}
	public void setSource(Value source) {
		this.source = source;
	}
	public Set<Value> getAliasSet() {
		return aliasSet;
	}
	public void setAliasSet(Set<Value> aliasSet) {
		this.aliasSet = aliasSet;
	}
	
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

	public void setCorrespondingMethod(SootMethod correspondingMethod) {
		this.correspondingMethod = correspondingMethod;
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
