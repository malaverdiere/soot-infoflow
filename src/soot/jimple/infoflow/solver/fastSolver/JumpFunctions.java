/*******************************************************************************
 * Copyright (c) 2012 Eric Bodden.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eric Bodden - initial API and implementation
 ******************************************************************************/
package soot.jimple.infoflow.solver.fastSolver;

import heros.SynchronizedBy;
import heros.ThreadSafe;
import heros.solver.PathEdge;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


/**
 * The IDE algorithm uses a list of jump functions. Instead of a list, we use a set of three
 * maps that are kept in sync. This allows for efficient indexing: the algorithm accesses
 * elements from the list through three different indices.
 */
@ThreadSafe
public class JumpFunctions<N,D> {
	
	//mapping from target node and value to a list of all source values and associated functions
	//where the list is implemented as a mapping from the source value to the function
	//we exclude empty default functions
	@SynchronizedBy("consistent lock on this")
	protected Table<N,D,Set<D>> nonEmptyReverseLookup = HashBasedTable.create();
	
	public JumpFunctions() {
	}

	/**
	 * Records a jump function. The source statement is implicit.
	 * @see PathEdge
	 */
	public synchronized boolean addFunction(D sourceVal, N target, D targetVal) {
		assert sourceVal!=null;
		assert target!=null;
		assert targetVal!=null;
		
		Set<D> sourceValToFunc = nonEmptyReverseLookup.get(target, targetVal);
		if(sourceValToFunc==null) {
			sourceValToFunc = new HashSet<D>();
			nonEmptyReverseLookup.put(target,targetVal,sourceValToFunc);
		}
		return sourceValToFunc.add(sourceVal);
	}
	
	/**
     * Returns, for a given target statement and value all associated
     * source values, and for each the associated edge function.
     * The return value is a mapping from source value to function.
	 */
	public synchronized Set<D> reverseLookup(N target, D targetVal) {
		assert target!=null;
		assert targetVal!=null;
		Set<D> res = nonEmptyReverseLookup.get(target,targetVal);
		if(res==null) return Collections.emptySet();
		return res;
	}
	
	/**
	 * Removes a jump function. The source statement is implicit.
	 * @see PathEdge
	 * @return True if the function has actually been removed. False if it was not
	 * there anyway.
	 */
	public synchronized boolean removeFunction(D sourceVal, N target, D targetVal) {
		assert sourceVal!=null;
		assert target!=null;
		assert targetVal!=null;
		
		Set<D> sourceValToFunc = nonEmptyReverseLookup.get(target, targetVal);
		if (sourceValToFunc == null)
			return false;
		if (!sourceValToFunc.remove(sourceVal))
			return false;
		if (sourceValToFunc.isEmpty())
			nonEmptyReverseLookup.remove(targetVal, targetVal);
		
		return true;
	}
	
	/**
	 * Checks whether the given fact is already in the jump function table
	 * @return True if the edge is in the table, otherwise false
	 */
	public synchronized boolean containsFact(D sourceVal, N target, D targetVal) {
		Set<D> res = nonEmptyReverseLookup.get(target, targetVal);
		return res == null ? false : res.contains(sourceVal);
	}

	/**
	 * Removes all jump functions
	 */
	public synchronized void clear() {
		this.nonEmptyReverseLookup.clear();
	}

}
