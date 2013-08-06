package soot.jimple.infoflow.ipc;

import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public interface IIPCManager {
    
    /**
     * determines if a method called by the Stmt is an IPC method or not
     * @param sCallSite a Stmt which should include an invokeExrp calling a method
     * @param cfg the interprocedural controlflow graph
     * @return true if source method is called
     */
    public abstract boolean isIPC(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg);

}
