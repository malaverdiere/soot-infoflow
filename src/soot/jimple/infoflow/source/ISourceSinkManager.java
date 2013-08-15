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
package soot.jimple.infoflow.source;

import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
/**
 * the SourceSinkManager can tell if a statement contains a source or a sink
 */
public interface ISourceSinkManager {

	/**
	 * determines if a method called by the Stmt is a source method or not
	 * @param sCallSite a Stmt which should include an invokeExrp calling a method
	 * @param cfg the interprocedural controlflow graph
	 * @return true if source method is called
	 */
	public abstract boolean isSource(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg);
	/**
	 * determines if a method called by the Stmt is a sink method or not
	 * @param sCallSite a Stmt which should include an invokeExrp calling a method
	 * @param cfg the interprocedural controlflow graph
	 * @return true if sink method is called
	 */
	public abstract boolean isSink(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg);

}
