package soot.jimple.infoflow.data;

import soot.EquivalentValue;
import soot.Local;
import soot.SootField;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

public class AccessPath {
	private Value value;
	private SootField field;
	
	public AccessPath(Value val){
		assert !(val instanceof EquivalentValue);
		if(val instanceof StaticFieldRef){
			StaticFieldRef ref = (StaticFieldRef) val;
			field = ref.getField();
		} else if(val instanceof InstanceFieldRef){
			InstanceFieldRef ref = (InstanceFieldRef) val;
			value = ref.getBase();
			field = ref.getField();
		}else{
			value = val;
		}
	}
	

	
	public AccessPath(SootField staticfield){
		field = staticfield;
	}
	
	public AccessPath(Value base, SootField field){
		value = base;
		this.field = field;
	}

	
	/**
	 * replaces value and returns it if matches with val, otherwise original is returned
	 * @param val
	 * @param replacement
	 * @return
	 */
	public AccessPath replace(Value val, Value replacement){
		if(val instanceof Local){
			if(val.equals(value)){
				return new AccessPath(replacement);
			}
		} 
		return this;
	}
		
	public Value getPlainValue() {
		if(value == null){
			return null;
		}
		return value;
	}
	
	public void setValue(Value value) {
		this.value = value;
	}
	
	
	public SootField getField() {
		return field;
	}
	public void setField(SootField field) {
		this.field = field;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AccessPath other = (AccessPath) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	public boolean isStaticFieldRef(){
		if(value == null && field != null){
			return true;
		}
		return false;
	}
	
	public boolean isInstanceFieldRef(){
		if(value != null && field != null){
			return true;
		}
		return false;
	}
	
	public boolean isLocal(){
		if(value != null && value instanceof Local && field == null){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		String str = "";
		if(value != null){
			str += value.toString() +"(" + value.getType() +")" + " ";
		}
		if(field != null){
			str += field.toString();
		}
		
		return str;
	}
	
	public AccessPath copyWithNewValue(Value val){
		if(val instanceof Local){
			AccessPath a = new AccessPath(val);
			a.field = this.field;
			return a;
		}else{
			return new AccessPath(val);
		}
	}

}
