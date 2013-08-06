package soot.jimple.infoflow.ipc;

import heros.InterproceduralCFG;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

/**
 * Abstracts from the very generic statement-based IPCManager so that users
 * can conveniently work on the called methods instead of having to analyze the
 * call statement every time
 * 
 * @author Steven Arzt
 *
 */
public abstract class MethodBasedIPCManager implements IIPCManager {

    public abstract boolean isIPCMethod(SootMethod method);
    
    @Override
    public boolean isIPC(Stmt sCallSite, InterproceduralCFG<Unit, SootMethod> cfg) {
        assert sCallSite != null;
        return sCallSite.containsInvokeExpr()
                && isIPCMethod(sCallSite.getInvokeExpr().getMethod());
    }

}
