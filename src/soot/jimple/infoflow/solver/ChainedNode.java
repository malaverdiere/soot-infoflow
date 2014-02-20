/*******************************************************************************
 * Copyright (c) 2014 Eric Bodden.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eric Bodden - initial API and implementation
 ******************************************************************************/
package soot.jimple.infoflow.solver;

/**
 * A data flow fact that can be chained to build a path of facts from a source
 * to a sink.
 * 
 * @author Steven Arzt
 */
public interface ChainedNode<D> {
	
	/**
	 * Sets the predecessor before the current method call. This method gets
	 * called when returning from methods so that the path can be adapted to
	 * skip over the method and directly connect to the node before the call.
	 * @param predecessor The predecessor fact before the method call
	 */
	public D setJumpPredecessor(D predecessor);

}
