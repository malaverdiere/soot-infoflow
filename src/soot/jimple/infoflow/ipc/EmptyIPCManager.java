package soot.jimple.infoflow.ipc;

import soot.SootMethod;
import soot.jimple.infoflow.source.ISourceSinkManager;

/**
 * A {@link ISourceSinkManager} that always returns false, i.e. one for which
 * there are no sources or sinks at all.
 * 
 * @author Steven Arzt
 */
public class EmptyIPCManager extends MethodBasedIPCManager {

	public EmptyIPCManager(){
	}
	
    @Override
    public boolean isIPCMethod(SootMethod method) {
        return false;
    }

}
