package soot.jimple.infoflow.data;


import java.util.LinkedList;
import java.util.Stack;

import soot.SootField;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

public class Abstraction {
	private final AccessPath accessPath;
	private final Value source;
	private final Stmt sourceContext;
	private int hashCode;
	private Stack<Unit> callStack;
	

	public Abstraction(Value taint, Value src, Stmt srcContext){
		source = src;
		accessPath = new AccessPath(taint);
		callStack = new Stack<Unit>();
		sourceContext = srcContext;
	}
	
	//TODO: make private and change AwP
	protected Abstraction(AccessPath p, Value src, Stmt srcContext){
		source = src;
		sourceContext = srcContext;
		accessPath = p;
		callStack = new Stack<Unit>();
	}
	
	
	public Abstraction deriveNewAbstraction(AccessPath p){
		Abstraction a = new Abstraction(p, source, sourceContext);
		a.callStack.addAll(this.callStack);
		return a;
	}
	
	public Abstraction deriveNewAbstraction(Value taint){
		return this.deriveNewAbstraction(taint, false);
	}
	
	public Abstraction deriveNewAbstraction(Value taint, boolean cutFirstField){
		Abstraction a;
		if(cutFirstField){
			LinkedList<SootField> tempList = new LinkedList<SootField>(accessPath.getFields());
			tempList.removeFirst();
			a = new Abstraction(new AccessPath(taint, tempList), source, sourceContext);
		}
		else
			a = new Abstraction(new AccessPath(taint,accessPath.getFields()), source, sourceContext);		
		a.callStack.addAll(this.callStack);
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
		callStack = new Stack<Unit>();
		if (original == null) {
			source = null;
			sourceContext = null;
		}
		else {
			source = original.source;
			sourceContext = original.sourceContext;
			callStack.addAll(original.callStack);
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
		if (callStack == null) {
			if (other.callStack != null)
				return false;
		} else if (!callStack.equals(other.callStack))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		if(hashCode == 0){
			int result = 1;
			result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((sourceContext == null) ? 0 : sourceContext.hashCode());
			hashCode = result;
		}
		//because the contents of callStack are not final we cannot 
		//cache this part of the hashCode calculation:
		return prime * hashCode + ((callStack == null) ? 0 : callStack.hashCode());
	}
	
	public void addToStack(Unit u){
		// Do not add a method we already have on the stack.
		// Otherwise, recursive calls will make our analysis run in an
		// infinite loop.
		if (!callStack.contains(u))
			callStack.push(u);
	}
	
	public void removeFromStack(){
		if(!callStack.isEmpty())
			callStack.pop();
	}
	
	public boolean isStackEmpty(){
		return callStack.isEmpty();
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

}
