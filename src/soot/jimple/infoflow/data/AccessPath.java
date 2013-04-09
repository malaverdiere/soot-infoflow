package soot.jimple.infoflow.data;

import java.util.LinkedList;

import soot.EquivalentValue;
import soot.Local;
import soot.SootField;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

public class AccessPath {
	private LinkedList<Value> values = new LinkedList<Value>();
	private SootField field;
	//in contrast to a certain value which is tainted unknownfieldtainted says that any (*) field of the value is tainted
	private boolean unknownfieldtainted; //also known as star/*

	
	public AccessPath(Value val){
		values.clear();
		assert !(val instanceof EquivalentValue);
		if(val instanceof StaticFieldRef){
			StaticFieldRef ref = (StaticFieldRef) val;
			field = ref.getField();
		} else if(val instanceof InstanceFieldRef){
			InstanceFieldRef ref = (InstanceFieldRef) val;
			values.add(ref.getBase());
			field = ref.getField();
		}else{
			values.add(val);
		}
		unknownfieldtainted = false;
	}
	
	
	public AccessPath(Value val, boolean fieldtainted){
		this(val);
		unknownfieldtainted = fieldtainted;
	}
	
	
	public AccessPath(SootField staticfield){
		field = staticfield;
	}
	
	public AccessPath(Value base, SootField field){
		values.clear();
		values.add(base);
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
			if(val.equals(values)){
				return new AccessPath(replacement);
			}
		} 
		return this;
	}
		
	public Value getPlainValue() {
		if(values.isEmpty()){
			return null;
		}
		return values.getLast();
	}
	
	public Local getPlainLocal(){
		if(!values.isEmpty() && values.getLast() instanceof Local){
			return (Local)values.getLast();
		}
		return null;
	}
	
	public void addValue(Value value) {
		this.values.add(value);
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
		result = prime * result + (unknownfieldtainted ? 1231 : 1237);
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		if (unknownfieldtainted != other.unknownfieldtainted)
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}
	
	public boolean isStaticFieldRef(){
		if(values.isEmpty() && field != null){
			assert (field.makeRef() instanceof StaticFieldRef || field.makeRef().isStatic());
			return true;
		}
		return false;
	}
	
	public boolean isInstanceFieldRef(){
		if(!values.isEmpty() && field != null){
			return true;
		}
		return false;
	}
	
	/**
	 * only fields (*) are tainted, not the object itself 
	 * @return
	 */
	public boolean isOnlyFieldsTainted(){
		return unknownfieldtainted;
	}
	
	public boolean isLocal(){
		if(!values.isEmpty() && values.getLast() instanceof Local && field == null){
			return true;
		}
		return false;
	}
	
	@Override
	public String toString(){
		String str = "";
		if(!values.isEmpty()){
			str += values.toString() +"(" + values.getLast().getType() +")" + " ";
		}
		if(field != null){
			str += field.toString();
		}
		if(unknownfieldtainted)
			str += ".*";
		return str;
	}
	
	public AccessPath copyWithNewValue(Value val){
		if(val instanceof Local){
			AccessPath a = new AccessPath(val);
			a.field = this.field;
			a.unknownfieldtainted = this.unknownfieldtainted;
			return a;
		}else{
			if(val instanceof InstanceFieldRef && field != null){
				AccessPath a = new AccessPath(val);
				a.unknownfieldtainted = true;
				return a;
			}
			
			return new AccessPath(val);
		}
	}

}
