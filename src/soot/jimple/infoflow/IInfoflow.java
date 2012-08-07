package soot.jimple.infoflow;

import java.util.List;

import soot.jimple.infoflow.data.AnalyzeClass;

public interface IInfoflow {

	public void computeInfoflow(List<AnalyzeClass> classes);
	
	
	public void computeInfoflow(String classNameWithPath, boolean hasMainMethod, List<String> entryMethodNames);
}
