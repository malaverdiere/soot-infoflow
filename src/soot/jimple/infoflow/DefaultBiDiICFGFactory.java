/**
 * (c) Copyright 2013, Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.infoflow;


import soot.jimple.infoflow.heros.InfoflowCFG;
import soot.jimple.infoflow.heros.PDomICFG;

public class DefaultBiDiICFGFactory implements BiDirICFGFactory {

    @Override
    public PDomICFG buildBiDirICFG(){
        return new InfoflowCFG();
    }
}
