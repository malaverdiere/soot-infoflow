package soot.jimple.infoflow.data;

import java.util.Arrays;

import soot.Local;
import soot.SootField;
import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.infoflow.Infoflow;
/**
 * This class represents the taint, containing a base value and a list of fields (length is bounded by Infoflow.ACCESSPATHLENGTH)
 *  
 *
 */
public class AccessPath implements Cloneable {
	
	// ATTENTION: This class *must* be immutable!
	/*
	 * tainted value, is not null for non-static values
	 */
	private final Value value;
	/**
	 * list of fields, either they are based on a concrete @value or they indicate a static field
	 */
	private final SootField[] fields;
	private int hashCode = 0;

	public AccessPath(Value val){
		this(val, (SootField[]) null);
	}
	
	protected AccessPath(Value val, SootField[] appendingFields){
		assert (val == null && appendingFields != null && appendingFields.length > 0)
		 	|| val instanceof Local
			|| val instanceof InstanceFieldRef
			|| val instanceof StaticFieldRef;

		SootField baseField = null;
		if(val instanceof StaticFieldRef){
			StaticFieldRef ref = (StaticFieldRef) val;
			value = null;
			baseField = ref.getField();
		}
		else if(val instanceof InstanceFieldRef){
			InstanceFieldRef ref = (InstanceFieldRef) val;
			value = ref.getBase();
			baseField = ref.getField();
		}
		else {
			value = val;
		}

		int fieldNum = (baseField == null ? 0 : 1)
				+ (appendingFields == null ? 0 : appendingFields.length);
		this.fields = new SootField[Math.min(Infoflow.getAccessPathLength(), fieldNum)];
		
		if (baseField != null)
			this.fields[0] = baseField;

		if (appendingFields != null)
			for (int i = (baseField == null ? 0 : 1); i < this.fields.length; i++)
				this.fields[i] = appendingFields[i - (baseField == null ? 0 : 1)];
	}
	
	public AccessPath(SootField staticfield){
		this.fields = new SootField[] { staticfield };
		value = null;
	}
	
	public AccessPath(Value base, SootField field){
		this(base, new SootField[] { field });
		assert base instanceof Local;
	}
		
	public Value getPlainValue() {
		return value;
	}
	
	public Local getPlainLocal(){
		if(value != null && value instanceof Local){
			return (Local)value;
		}
		return null;
	}
	
	public SootField getLastField() {
		if (fields == null || fields.length == 0)
			return null;
		return fields[fields.length - 1];
	}
	
	public SootField getFirstField(){
		if (fields == null || fields.length == 0)
			return null;
		return fields[0];
	}
	
	protected SootField[] getFields(){
		return fields;
	}
	
	@Override
	public int hashCode() {
		if (hashCode == 0) {
			final int prime = 31;
			this.hashCode = 1;
			this.hashCode = prime * this.hashCode + ((fields == null) ? 0 : Arrays.hashCode(fields));
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
		if (!Arrays.equals(fields, other.fields))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		
		return true;
	}
	
	public boolean isStaticFieldRef(){
		if(value == null && fields != null && fields.length > 0){
			assert (getFirstField().makeRef() instanceof StaticFieldRef || getFirstField().makeRef().isStatic()) : "Assertion failed for fields: " + fields.toString();
			return true;
		}
		return false;
	}
	
	public boolean isInstanceFieldRef(){
		return value != null && fields != null && fields.length > 0;
	}
	
	
	public boolean isLocal(){
		return value != null && value instanceof Local && (fields == null || fields.length == 0);
	}
	
	@Override
	public String toString(){
		String str = "";
		if(value != null)
			str += value.toString() +"(" + value.getType() +")";
		if (fields != null)
			for (int i = 0; i < fields.length; i++)
				if (fields[i] != null) {
					if (!str.isEmpty())
						str += " ";
					str += fields[i].toString();
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
	
	@Override
	public AccessPath clone(){
		AccessPath a = new AccessPath(value, fields);
		assert a.equals(this);
		return a;
	}

}
