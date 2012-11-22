package soot.jimple.infoflow.data;

import java.util.HashSet;
import java.util.Set;

import soot.SootMethod;
import soot.Value;

public class Abstraction {
	private Value taintedObject;
	private Value source;
	private Set<Value> aliasSet;
	private SootMethod correspondingMethod;
	//private List<Value> historieValues;
	
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
		aliasSet.add(val);
	}

	public SootMethod getCorrespondingMethod() {
		return correspondingMethod;
	}

	public void setCorrespondingMethod(SootMethod correspondingMethod) {
		this.correspondingMethod = correspondingMethod;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj instanceof Abstraction){
			Abstraction abs = (Abstraction) obj;
			if(abs.getSource() == null && source == null){
				if(abs.getTaintedObject() == null && taintedObject == null){
					return true;
				}
				if(abs.getTaintedObject() != null && abs.getTaintedObject().equals(taintedObject)){
					return true;
				}
				
			}
			if(abs.getSource() != null && abs.getSource().equals(source)){
				if(abs.getTaintedObject() == null && taintedObject == null){
					return true;
				}
				if(abs.getTaintedObject() != null && abs.getTaintedObject().equals(taintedObject)){
					return true;
				}
				
				
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		return 13* (((source!= null)?source.hashCode():7) + ((taintedObject != null)?taintedObject.hashCode():17));
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
