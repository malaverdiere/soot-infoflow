package soot.jimple.infoflow.data;

import java.util.LinkedList;

import soot.EquivalentValue;
import soot.Local;
import soot.SootField;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

public class AccessPath {
	public static final int ACCESSPATHLENGTH = 5;
	
	private final Value value;
	private final LinkedList<SootField> fields = new LinkedList<SootField>();

	public AccessPath(Value val){
		assert !(val instanceof EquivalentValue);
		if(val instanceof StaticFieldRef){
			StaticFieldRef ref = (StaticFieldRef) val;
			if(fields.size()< ACCESSPATHLENGTH)
				fields.add(ref.getField());
			value = null;
		} else if(val instanceof InstanceFieldRef){
			InstanceFieldRef ref = (InstanceFieldRef) val;
			value = ref.getBase();
			if(fields.size()< ACCESSPATHLENGTH)
				fields.add(ref.getField());
		}else{
			value = val;
		}
	}
	
	
	protected AccessPath(Value val, LinkedList<SootField> appendingFields){
		this(val);
		if(appendingFields.size() + this.fields.size() <= ACCESSPATHLENGTH)
			fields.addAll(appendingFields);
		else{
			//cut it:
			int pos = 0;
			while(fields.size() < ACCESSPATHLENGTH){
				fields.add(appendingFields.get(pos));
				pos++;
			}	
		}
	}
	
	public AccessPath(SootField staticfield){
		fields.add(staticfield);
		value = null;
	}
	
	public AccessPath(Value base, SootField field){
		value = base;
		if(fields.size()< ACCESSPATHLENGTH)
			fields.add(field);
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
	
	public SootField getLastField() {
		return fields.getLast();
	}
	
	public SootField getFirstField(){
		return fields.getFirst();
	}
	
	public LinkedList<SootField> getFields(){
		return fields;
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
		if (super.equals(obj))
			return true;
		if (obj == null || !(obj instanceof AccessPath))
			return false;
		AccessPath other = (AccessPath) obj;
		if (!fields.equals(other.fields))
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
			a.fields.addAll(this.fields);
			return a;
		}else{
			if(val instanceof InstanceFieldRef){
				AccessPath a = new AccessPath(val);
				if(a.fields.size() + this.fields.size() <= ACCESSPATHLENGTH)
					a.fields.addAll(this.fields);
				else{
					//cut it:
					int pos = 0;
					while(a.fields.size() < ACCESSPATHLENGTH){
						a.fields.add(this.fields.get(pos));
						pos++;
					}
					
				}
				return a;
			}
			//staticfieldref etc:
			return new AccessPath(val);
		}
	}

}
