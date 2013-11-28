/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow.data;

import java.util.Arrays;

import soot.Local;
import soot.SootField;
import soot.Value;
import soot.jimple.ArrayRef;
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

	/**
	 * The empty access path denotes a code region depending on a tainted
	 * conditional. If a function is called inside the region, there is no
	 * tainted value inside the callee, but there is taint - modeled by
	 * the empty access path.
	 */
	private static final AccessPath emptyAccessPath = new AccessPath();

	private AccessPath() {
		this.value = null;
		this.fields = null;
	}

	public AccessPath(Value val){
		this(val, (SootField[]) null);
	}
	
	public AccessPath(Value val, SootField[] appendingFields){
		assert (val == null && appendingFields != null && appendingFields.length > 0)
		 	|| canContainValue(val);

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
		else if (val instanceof ArrayRef) {
			ArrayRef ref = (ArrayRef) val;
			value = ref.getBase();
		}
		else {
			value = val;
		}

		int fieldNum = (baseField == null ? 0 : 1)
				+ (appendingFields == null ? 0 : appendingFields.length);
		fieldNum = Math.min(Infoflow.getAccessPathLength(), fieldNum);
		if (fieldNum == 0)
			this.fields = null;
		else {
			this.fields = new SootField[fieldNum];
			if (baseField != null)
				this.fields[0] = baseField;
			if (appendingFields != null)
				for (int i = (baseField == null ? 0 : 1); i < this.fields.length; i++)
					this.fields[i] = appendingFields[i - (baseField == null ? 0 : 1)];
		}
	}
	
	public AccessPath(SootField staticfield){
		this.fields = new SootField[] { staticfield };
		value = null;
	}
	
	public AccessPath(Value base, SootField field){
		this(base, new SootField[] { field });
		assert base instanceof Local;
	}
	
	/**
	 * Checks whether the given value can be the base value value of an access
	 * path
	 * @param val The value to check
	 * @return True if the given value can be the base value value of an access
	 * path
	 */
	public static boolean canContainValue(Value val) {
		return val instanceof Local
				|| val instanceof InstanceFieldRef
				|| val instanceof StaticFieldRef
				|| val instanceof ArrayRef;
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
	
	public int getFieldCount() {
		return fields == null ? 0 : fields.length;
	}
	
	@Override
	public int hashCode() {
		if (hashCode != 0)
			return hashCode;
		
		synchronized (this) {
			final int prime = 31;
			this.hashCode = 1;
			this.hashCode = prime * this.hashCode + ((fields == null) ? 0 : Arrays.hashCode(fields));
			this.hashCode = prime * this.hashCode + ((value == null) ? 0 : value.hashCode());
			return this.hashCode;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this || super.equals(obj))
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
		
		assert this.hashCode() == obj.hashCode();
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
		if (this.value != null && this.value.equals(val))
			return this;
		
		return new AccessPath(val, this.fields);
	}
	
	@Override
	public AccessPath clone(){
		// The empty access path is a singleton
		if (this == emptyAccessPath)
			return this;

		AccessPath a = new AccessPath(value, fields);
		assert a.equals(this);
		return a;
	}

	public static AccessPath getEmptyAccessPath() {
		return emptyAccessPath;
	}
	
	public boolean isEmpty() {
		return value == null && (fields == null || fields.length == 0);
	}

	/**
	 * Checks whether this access path entails the given one, i.e. refers to all
	 * objects the other access path also refers to.
	 * @param a2 The other access path
	 * @return True if this access path refers to all objects the other access
	 * path also refers to
	 */
	public boolean entails(AccessPath a2) {
		if (this.isEmpty() || a2.isEmpty())
			return false;
		
		if ((this.value != null && a2.value == null)
				|| (this.value == null && a2.value != null))
			return false;
		if (this.value != null && !this.value.equals(a2.value))
			return false;
		
		if (this.fields.length > a2.fields.length)
			return false;
		for (int i = 0; i < this.fields.length; i++)
			if (!this.fields[i].equals(a2.fields[i]))
				return false;
		return true;
	}
	
	/**
	 * Merges this access path with the given one, i.e., adds the fields of the
	 * given access path to this one.
	 * @param ap The access path whose fields to append to this one
	 * @return The new access path
	 */
	public AccessPath merge(AccessPath ap) {
		int offset = this.fields == null ? 0 : this.fields.length;
		SootField[] fields = new SootField[offset + (ap.fields == null ? 0 : ap.fields.length)];
		if (this.fields != null)
			for (int i = 0; i < this.fields.length; i++)
				fields[i] = this.fields[i];
		if (ap.fields != null)
			if (ap.fields != null && ap.fields.length > 0)
				for (int i = 0; i < ap.fields.length; i++)
					fields[offset + i] = ap.fields[i];
		
		return new AccessPath(this.value, fields);
	}
	
	public AccessPath dropLastField() {
		if (fields == null || fields.length == 0)
			return this;
		SootField[] newFields = new SootField[fields.length - 1];
		System.arraycopy(fields, 0, newFields, 0, fields.length - 1);
		return new AccessPath(value, newFields);
	}

}
