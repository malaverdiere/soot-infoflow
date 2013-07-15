package soot.jimple.infoflow.entryPointCreators;

import java.util.List;

import soot.SootMethod;

public interface IEntryPointCreator {

	/**
	 * Generates a dummy main method that calls all methods in the given list
	 * @param methods The methods to call in the generated dummy main method.
	 * Methods are given in Soot's signature syntax.
	 * @return The generated main method
	 */
	public SootMethod createDummyMain(List<String> methods);
	
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
