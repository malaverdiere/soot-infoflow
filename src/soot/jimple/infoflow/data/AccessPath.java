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
	
	protected AccessPath(Value val, SootField[] appendingFields){
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

}
