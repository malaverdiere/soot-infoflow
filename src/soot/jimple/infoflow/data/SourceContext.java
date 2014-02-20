package soot.jimple.infoflow.data;

import soot.Value;
import soot.jimple.Stmt;

/**
 * Class representing a source value together with the statement that created it
 * 
 * @author Steven Arzt
 */
public class SourceContext implements Cloneable {
	private final Value value;
	private final Stmt stmt;
	private final Object userData;
	
	public SourceContext(Value value, Stmt stmt) {
		assert value != null;
		
		this.value = value;
		this.stmt = stmt;
		this.userData = null;
	}
	
	public SourceContext(Value value, Stmt stmt, Object userData) {
		assert value != null;

		this.value = value;
		this.stmt = stmt;
		this.userData = userData;
	}
	
	public Value getValue() {
		return this.value;
	}
	
	public Stmt getStmt() {
		return this.stmt;
	}
	
	public Object getUserData() {
		return this.userData;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stmt == null) ? 0 : stmt.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result + ((userData == null) ? 0 : userData.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		SourceContext other = (SourceContext) obj;
		if (stmt == null) {
			if (other.stmt != null)
				return false;
		} else if (!stmt.equals(other.stmt))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (userData == null) {
			if (other.userData != null)
				return false;
		} else if (!userData.equals(other.userData))
			return false;
		return true;
	}
	
	@Override
	public SourceContext clone() {
		SourceContext sc = new SourceContext(value, stmt, userData);
		assert sc.equals(this);
		return sc;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
