package soot.jimple.infoflow.data;

import soot.EquivalentValue;
import soot.Local;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

public class AccessPath {
	private EquivalentValue value;
	private String field;
	
	public AccessPath(Value val){
		assert !(val instanceof EquivalentValue);
		if(val instanceof StaticFieldRef){
			StaticFieldRef ref = (StaticFieldRef) val;
			field = ref.getFieldRef().declaringClass().getName() + "."+ref.getFieldRef().name();
		} else if(val instanceof InstanceFieldRef){
			InstanceFieldRef ref = (InstanceFieldRef) val;
			value = new EquivalentValue(ref.getBase());
			field = ref.getField().getName();
		}else{
			value = new EquivalentValue(val);
		}
	}
	
	public AccessPath(EquivalentValue val){
		if(val.getValue() instanceof StaticFieldRef){
			StaticFieldRef ref = (StaticFieldRef) val.getValue();
			field = ref.getFieldRef().declaringClass().getName() + "."+ref.getFieldRef().name();
		} else if(val.getValue() instanceof InstanceFieldRef){
			InstanceFieldRef ref = (InstanceFieldRef) val.getValue();
			value = new EquivalentValue(ref.getBase());
			field = ref.getField().getName();
		}else{
			value = val;
		}
		
	}
	
	public AccessPath(String staticfieldref){
		field = staticfieldref;
	}
	
	public AccessPath(Value base, String field){
		value = new EquivalentValue(base);
		this.field = field;
	}
	
	public AccessPath(EquivalentValue base, String field){
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
	
	public EquivalentValue getValue() {
		return value;
	}
	
	public Value getPlainValue() {
		if(value == null){
			return null;
		}
		return value.getValue();
	}
	
	public void setValue(Value value) {
		this.value = new EquivalentValue(value);
	}
	
	public void setValue(EquivalentValue value) {
		this.value = value;
	}
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
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
		if(value != null && value.getValue() instanceof Local && field == null){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		String str = "";
		if(value != null){
			str += value.getValue().toString() +"(" + value.getValue().getType() +")" + " ";
		}
		if(field != null){
			str += field;
		}
		
		return str;
	}
	
	public AccessPath copyWithNewValue(Value val){
		if(val instanceof Local){
			AccessPath a = new AccessPath(val);
			a.field = this.field;
			return a;
		}else{
			assert this.field == null : "Warning: val "+ val + "/" + val.getType() + " field: "+ this.field + " oldval "+ this.value;
			return new AccessPath(val);
		}
	}

}
