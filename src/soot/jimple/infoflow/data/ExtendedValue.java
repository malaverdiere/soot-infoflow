package soot.jimple.infoflow.data;

import java.util.LinkedList;
import java.util.List;

import soot.Type;
import soot.UnitPrinter;
import soot.Value;
import soot.util.Switch;

public class ExtendedValue implements Value {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4396476847951832785L;
	private Value originalValue;
	private List<Value> historieValues;
	
	public ExtendedValue(Value val){
		originalValue = val;
		historieValues = new LinkedList<Value>();
		if(val instanceof ExtendedValue){
			for(Value v : ((ExtendedValue)val).getHistory()){
				historieValues.add(v);
			}
		}
	}
	
	public void addHistorie(Value val){
		historieValues.add(val);
	}
	
	public Value getOriginalValue(){
		return originalValue;
	}
	
	public List<Value> getHistory(){
		return historieValues;
	}
	
	public Object clone(){
		Value v = (Value) originalValue.clone();
		ExtendedValue e = new ExtendedValue(v);	
		return e;
		
	}
	
	@Override
	public void apply(Switch sw) {
		originalValue.apply(sw);
		
	}

	@Override
	public boolean equivTo(Object o) {
		return originalValue.equivTo(o);
		// TODO improve?
	
	}

	@Override
	public int equivHashCode() {
		return originalValue.equivHashCode();
		// TODO improve?
		
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getUseBoxes() {
		return originalValue.getUseBoxes();
	}

	@Override
	public Type getType() {
		return originalValue.getType();
	}

	@Override
	public void toString(UnitPrinter up) {
		originalValue.toString(up);
		
	}
	
	@Override
	public String toString(){
		return originalValue.toString();
	}

}
