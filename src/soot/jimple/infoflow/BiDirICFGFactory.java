/**
 * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.infoflow;


import soot.jimple.infoflow.heros.IInfoflowCFG;

public interface BiDirICFGFactory {

    public IInfoflowCFG buildBiDirICFG();

}
