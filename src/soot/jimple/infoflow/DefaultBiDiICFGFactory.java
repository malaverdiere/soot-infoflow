/**
 * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.infoflow;


import soot.jimple.infoflow.heros.IInfoflowCFG;
import soot.jimple.infoflow.heros.InfoflowCFG;

public class DefaultBiDiICFGFactory implements BiDirICFGFactory {

    @Override
    public IInfoflowCFG buildBiDirICFG(){
        return new InfoflowCFG();
    }
}
