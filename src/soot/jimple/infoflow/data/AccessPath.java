package soot.jimple.infoflow.data;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import soot.EquivalentValue;
import soot.Local;
import soot.SootField;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

public class AccessPath {
	public static final int ACCESSPATHLENGTH = 5;

	// ATTENTION: This class *must* be immutable!
	private final Value value;
	private final List<SootField> fields;
	private int hashCode = 0;

	public AccessPath(Value val){
		this(val, Collections.<SootField>emptyList());
	}
	
	protected AccessPath(Value val, Collection<SootField> appendingFields){
		assert !(val instanceof EquivalentValue);

		List<SootField> fields = new LinkedList<SootField>();
		if(val instanceof StaticFieldRef){
			StaticFieldRef ref = (StaticFieldRef) val;
			if(fields.size()< ACCESSPATHLENGTH)
				fields.add(ref.getField());
			value = null;
		}
		else if(val instanceof InstanceFieldRef){
			InstanceFieldRef ref = (InstanceFieldRef) val;
			value = ref.getBase();
			if(fields.size() < ACCESSPATHLENGTH)
				fields.add(ref.getField());
		}
		else
			value = val;

		int cnt = appendingFields.size();
		for (SootField field : appendingFields)
			if (cnt < ACCESSPATHLENGTH) {
				fields.add(field);
				cnt++;
			}
			else
				break;
		this.fields = Collections.unmodifiableList(fields);
	}
	
	public AccessPath(SootField staticfield){
		this.fields = Collections.singletonList(staticfield);
		value = null;
	}
	
	public AccessPath(Value base, SootField field){
		value = base;
		List<SootField> fields = new LinkedList<SootField>();
		if(fields.size() < ACCESSPATHLENGTH)
			fields.add(field);
		this.fields = Collections.unmodifiableList(fields);
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
		if (fields.isEmpty())
			return null;
		return fields.get(fields.size() - 1);
	}
	
	public SootField getFirstField(){
		if (fields.isEmpty())
			return null;
		return fields.get(0);
	}
	
	protected Collection<SootField> getFields(){
		return fields;
	}
	
	@Override
	public int hashCode() {
		if (hashCode == 0) {
			final int prime = 31;
			this.hashCode = 1;
			this.hashCode = prime * this.hashCode + ((fields == null) ? 0 : fields.hashCode());
			this.hashCode = prime * this.hashCode + ((value == null) ? 0 : value.hashCode());
		}
		return this.hashCode;
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
			assert (getFirstField().makeRef() instanceof StaticFieldRef || getFirstField().makeRef().isStatic()) : "Assertion failed for fields: " + fields.toString();
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
		return new AccessPath(val, this.fields);
	}

}
