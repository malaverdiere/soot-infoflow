/*******************************************************************************
 * Copyright (c) 2013 Eric Bodden.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eric Bodden - initial API and implementation
 ******************************************************************************/
package soot.jimple.infoflow.solver.fastSolver;

import heros.IFDSTabulationProblem;
import heros.InterproceduralCFG;
import heros.solver.LinkedNode;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * An {@link IFDSSolver} that tracks paths for reporting. To do so, it requires that data-flow abstractions implement the LinkedNode interface.
 * The solver implements a cache of data-flow facts for each statement and source value. If for the same statement and source value the same
 * target value is seen again (as determined through a cache hit), then the solver propagates the cached value but at the same time links
 * both target values with one another.
 *  
 * @author Eric Bodden
 * @author Steven Arzt
 */
public class PathTrackingIFDSSolver<N, D extends LinkedNode<D>, M, I extends InterproceduralCFG<N, M>> extends IFDSSolver<N, D, M, I> {

	public PathTrackingIFDSSolver(IFDSTabulationProblem<N, D, M, I> ifdsProblem) {
		super(ifdsProblem);
	}

	protected final Map<CacheEntry, LinkedNode<D>> cache = Maps.newHashMap();
	
	@Override
	protected void propagate(D sourceVal, N target, D targetVal, N relatedCallSite, boolean isUnbalancedReturn) {
		CacheEntry currentCacheEntry = new CacheEntry(target, sourceVal, targetVal);

		boolean propagate = false;
		synchronized (this) {
			LinkedNode<D> existingTargetVal = cache.get(currentCacheEntry);
			if (existingTargetVal != null) {
				if (existingTargetVal != targetVal)
					existingTargetVal.addNeighbor(targetVal);
			} else {
				cache.put(currentCacheEntry, targetVal);
				propagate = true;
			}
		}

		if (propagate)
			super.propagate(sourceVal, target, targetVal, relatedCallSite, isUnbalancedReturn);
		
	};
	
	
	private class CacheEntry {
		private final N n;
		private final D sourceVal;
		private final D targetVal;
		
		private int hashCode = 0;

		public CacheEntry(N n, D sourceVal, D targetVal) {
			super();
			this.n = n;
			this.sourceVal = sourceVal;
			this.targetVal = targetVal;
		}

		@Override
		public int hashCode() {
			if (hashCode != 0)
				return hashCode;
			
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sourceVal == null) ? 0 : sourceVal.hashCode());
			result = prime * result + ((targetVal == null) ? 0 : targetVal.hashCode());
			result = prime * result + ((n == null) ? 0 : n.hashCode());
			hashCode = result;	// benign data race

			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings({ "unchecked" })
			CacheEntry other = (CacheEntry) obj;
			if (sourceVal == null) {
				if (other.sourceVal != null)
					return false;
			} else if (!sourceVal.equals(other.sourceVal))
				return false;
			if (targetVal == null) {
				if (other.targetVal != null)
					return false;
			} else if (!targetVal.equals(other.targetVal))
				return false;
			if (n == null) {
				if (other.n != null)
					return false;
			} else if (!n.equals(other.n))
				return false;
			return true;
		}
	}	
	


}
