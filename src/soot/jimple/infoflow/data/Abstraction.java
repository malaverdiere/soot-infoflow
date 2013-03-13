package soot.jimple.infoflow.data;


import soot.Value;

public class Abstraction {
	private final AccessPath accessPath;
	private final Value source;
	private int hashCode;
	

	public Abstraction(Value taint, Value src, boolean fieldtainted){
		source = src;
		accessPath = new AccessPath(taint, fieldtainted);
	}
	

//	protected Abstraction(AccessPath p, Value src, boolean fieldtainted){
//		source = src;
//		accessPath = new AccessPath(taint, fieldtainted);	
//	}
	
	//TODO: make private and change AwP
	protected Abstraction(AccessPath p, Value src){
		source = src;
		accessPath = p;
	}
	
	
	public Abstraction deriveNewAbstraction(AccessPath p){
		Abstraction a = new Abstraction(p, source);
		return a;
	}
	
	public Abstraction deriveNewAbstraction(Value taint, boolean fieldtainted){
		Abstraction a = new Abstraction(new AccessPath(taint, fieldtainted), source);
		return a;
	}
	
	public Value getSource() {
		return source;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Abstraction other = (Abstraction) obj;
		if (accessPath == null) {
			if (other.accessPath != null)
				return false;
		} else if (!accessPath.equals(other.accessPath))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		if(hashCode == 0){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			hashCode = result;
		}
		return hashCode;
	}
	
	@Override
	public String toString(){
		if(accessPath != null && source != null){
			return accessPath.toString() + " /source: "+ source.toString();
		}
		if(accessPath != null){
			return accessPath.toString();
		}
		return "Abstraction (null)";
	}
	
	public AccessPath getAccessPath(){
		return accessPath;
	}
}
