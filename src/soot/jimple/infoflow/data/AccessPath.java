package soot.jimple.infoflow.data;

import java.util.LinkedList;

import soot.EquivalentValue;
import soot.Local;
import soot.SootField;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

public class AccessPath {
	private Value value;
	private LinkedList<SootField> fields = new LinkedList<SootField>();

	
	public AccessPath(Value val){
		assert !(val instanceof EquivalentValue);
		if(val instanceof StaticFieldRef){
			StaticFieldRef ref = (StaticFieldRef) val;
			fields.add(ref.getField());
		} else if(val instanceof InstanceFieldRef){
			InstanceFieldRef ref = (InstanceFieldRef) val;
			value = ref.getBase();
			fields.add(ref.getField());
		}else{
			value = val;
		}
	}
	
	
	public AccessPath(Value val, LinkedList<SootField> appendingFields){
		this(val);
		fields.addAll(appendingFields);

	}
	
	
	public AccessPath(SootField staticfield){
		fields.add(staticfield);
	}
	
	public AccessPath(Value base, SootField field){
		value = base;
		this.fields.add(field);
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
	
	public Local getPlainLocal(){
		if(value != null && value instanceof Local){
			return (Local)value;
		}
		return null;
	}
	
	public void setValue(Value value) {
		this.value = value;
	}
	
	
	public SootField getLastField() {
		return fields.getLast();
	}
	
	public SootField getFirstField(){
		return fields.getFirst();
	}
	
	public LinkedList<SootField> getFields(){
		return fields;
	}
	
	public void addField(SootField field) {
		this.fields.add(field);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
		if (fields == null) {
			if (other.fields != null)
				return false;
		} else if (!fields.equals(other.fields))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	public boolean isStaticFieldRef(){
		if(value == null && !fields.isEmpty()){
			assert (fields.getFirst().makeRef() instanceof StaticFieldRef || fields.getFirst().makeRef().isStatic());
			return true;
		}
		return false;
	}
	
	public boolean isInstanceFieldRef(){
		if(value != null && !fields.isEmpty()){
			return true;
		}
		return false;
	}
	
	
	public boolean isLocal(){
		if(value != null && value instanceof Local && fields.isEmpty()){
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
		if(!fields.isEmpty()){
			str += fields.toString();
		}
		return str;
	}
	
	/**
	 * value val gets new base, fields are preserved.
	 * @param val
	 * @return
	 */
	public AccessPath copyWithNewValue(Value val){
		if(val instanceof Local){
			AccessPath a = new AccessPath(val);
			a.fields = (LinkedList<SootField>) this.fields.clone();
			return a;
		}else{
			if(val instanceof InstanceFieldRef){
				AccessPath a = new AccessPath(val);
				a.fields.addAll(this.fields);
				return a;
			}//TODO: staticfieldref how?
			
			return new AccessPath(val);
		}
	}

}
