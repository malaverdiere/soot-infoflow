package soot.jimple.interproc.ifds;

import java.util.List;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.Block;

/**
 * An interprocedural control-flow graph.
 * 
 * @param <N> Nodes in the CFG, typically {@link Unit} or {@link Block}
 * @param <M> Method representation
 */
public interface InterproceduralCFG<N,M>  {
	
	/**
	 * Returns the method containing a node.
	 */
	public M getMethodOf(N n);

	/**
	 * Returns the successor nodes.
	 */
	public List<N> getSuccsOf(N n);

	/**
	 * Returns all callee methods for a given call.
	 */
	public Set<M> getCalleesOfCallAt(N n);

	/**
	 * Returns all caller statements/nodes of a given method.
	 */
	public Set<N> getCallersOf(M m);

	/**
	 * Returns all call sites within a given method.
	 */
	public Set<N> getCallsFromWithin(M m);

	/**
	 * Returns all start points of a given method. There may be
	 * more than one start point in case of a backward analysis.
	 */
	public Set<N> getStartPointsOf(M m);

	/**
	 * Returns all statements to which a call could return.
	 * In the RHS paper, for every call there is just one return site.
	 * We, however, use as return site the successor statements, of which
	 * there can be many in case of exceptional flow.
	 */
	public List<N> getReturnSitesOfCallAt(N n);

	/**
	 * Returns <code>true</code> if the given statement is a call site.
	 */
	public boolean isCallStmt(N stmt);

	/**
	 * Returns <code>true</code> if the given statement leads to a method return
	 * (exceptional or not). For backward analyses may also be start statements.
	 */
	public boolean isExitStmt(N stmt);
	
	/**
	 * Returns true is this is a method's start statement. For backward analyses
	 * those may also be return or throws statements.
	 */
	public boolean isStartPoint(N stmt);
	
	/**
	 * Returns the set of all nodes that are neither call nor start nodes.
	 */
	public Set<N> allNonCallStartNodes();

}
