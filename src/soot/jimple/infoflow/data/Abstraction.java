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
	

}
