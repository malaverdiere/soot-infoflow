package soot.jimple.infoflow.data;


import java.util.Stack;

import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

public class Abstraction implements Cloneable {
	private final AccessPath accessPath;
	private final Value source;
	private final Stmt sourceContext;
	private int hashCode;
	private Stack<Unit> callStack;
	

	public Abstraction(Value taint, Value src, boolean fieldtainted, Stmt srcContext){
		source = src;
		accessPath = new AccessPath(taint, fieldtainted);
		callStack = new Stack<Unit>();
		sourceContext = srcContext;
	}
	

//	protected Abstraction(AccessPath p, Value src, boolean fieldtainted){
//		source = src;
//		accessPath = new AccessPath(taint, fieldtainted);	
//	}
	
	//TODO: make private and change AwP
	protected Abstraction(AccessPath p, Value src, Stmt srcContext){
		source = src;
		sourceContext = srcContext;
		accessPath = p;
		callStack = new Stack<Unit>();
	}
	
	public Abstraction deriveNewAbstraction(Unit u){
		Abstraction a = new Abstraction(accessPath, source, sourceContext);
		a.callStack = (Stack<Unit>) this.callStack.clone();
		a.addToStack(u);
		return a;
	}
	
	public Abstraction deriveNewAbstraction(AccessPath p){
		Abstraction a = new Abstraction(p, source, sourceContext);
		a.callStack = (Stack<Unit>) this.callStack.clone();
		return a;
	}
	
	public Abstraction deriveNewAbstraction(Value taint, boolean fieldtainted){
		Abstraction a = new Abstraction(new AccessPath(taint, fieldtainted), source, sourceContext);
		a.callStack = (Stack<Unit>) this.callStack.clone();
		return a;
	}
	
	/**
	 * Creates an abstraction as a copy of an existing abstraction,
	 * only exchanging the access path.
	 * @param p The value to be used as the new access path
	 * @param original The original abstraction to copy
	 */
	public Abstraction(Value p, Abstraction original){
		this(new AccessPath(p), original);
	}

	/**
	 * Creates an abstraction as a copy of an existing abstraction,
	 * only exchanging the access path.
	 * @param p The access path for the new abstraction
	 * @param original The original abstraction to copy
	 */
	public Abstraction(AccessPath p, Abstraction original){
		if (original == null) {
			source = null;
			sourceContext = null;
		}
		else {
			source = original.source;
			sourceContext = original.sourceContext;
		}
		accessPath = p;
	}

	public Value getSource() {
		return source;
	}

	public Stmt getSourceContext() {
		return this.sourceContext;
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
		if (callStack == null) {
			if (other.callStack != null)
				return false;
		} else if (!callStack.equals(other.callStack))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		if (sourceContext == null) {
			if (other.sourceContext != null)
				return false;
		} else if (!sourceContext.equals(other.sourceContext))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		if(hashCode == 0){
			final int prime = 31;
			int result = 1;
			result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
			result = prime * result + ((callStack == null) ? 0 : callStack.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((sourceContext == null) ? 0 : sourceContext.hashCode());
			hashCode = result;
		}
		return hashCode;
	}
	
	public void addToStack(Unit u){
		callStack.push(u);
	}
	
	public void removeFromStack(){
		if(!callStack.isEmpty())
			callStack.pop();
	}
	
	public Unit getElementFromStack(){
		if(!callStack.isEmpty())
			return callStack.peek();
		return null;
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
	
	@Override
	public Abstraction clone(){
		Abstraction a = new Abstraction(accessPath, source, sourceContext);
		a.callStack = (Stack<Unit>) this.callStack.clone();
		return a;
	}
	
}
