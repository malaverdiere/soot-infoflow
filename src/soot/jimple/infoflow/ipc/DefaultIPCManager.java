package soot.jimple.infoflow.ipc;

import java.util.List;

import soot.SootMethod;

/**
 * A {@link IIPCManager} working on lists of IPC methods
 * 
 * @author Steven Arzt
 */
public class DefaultIPCManager extends MethodBasedIPCManager {

	private List<String> ipcMethods;
	
	/**
	 * Creates a new instance of the {@link DefaultIPCManager} class
	 * @param ipcMethods The list of methods to be treated as IPCs
	 */
	public DefaultIPCManager(List<String> ipcMethods) {
		this.ipcMethods = ipcMethods;
	}

	
	/**
	 * Sets the list of methods to be treated as IPCs
	 * @param sources The list of methods to be treated as IPCs
	 */
	public void setSinks(List<String> ipcMethods){
		this.ipcMethods = ipcMethods;
	}
	
	@Override
	public boolean isIPCMethod(SootMethod sMethod) {
		return ipcMethods.contains(sMethod.toString());
	}


}
