package soot.jimple.infoflow.util;

import java.util.List;
import java.util.Map;

import soot.SootMethod;

public interface IEntryPointCreator {

	public SootMethod createDummyMain(Map<String, List<String>> classMap);
}
