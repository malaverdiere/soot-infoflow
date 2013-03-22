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
	
}
