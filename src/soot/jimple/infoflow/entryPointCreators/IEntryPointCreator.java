package soot.jimple.infoflow.entryPointCreators;

import java.util.List;
import java.util.Map;

import soot.SootMethod;

public interface IEntryPointCreator {

	public SootMethod createDummyMain(Map<String, List<String>> classMap);
	
	/**
	 * with this option enabled the EntryPointCreator tries to find suitable subclasses of abstract classes and implementers of interfaces
	 * @param b sets substitution of call parameters
	 */
	public void setSubstituteCallParams(boolean b);
	
	/**
	 * set classes that are allowed to substitute (otherwise constructor loops are very likely)
	 * @param l
	 */
	public void setSubstituteClasses(List<String> l);
}
