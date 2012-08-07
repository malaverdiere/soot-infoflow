package soot.jimple.infoflow;

import java.util.List;

public interface IInfoflow {

	public void computeInfoflow(String classNameWithPath, boolean hasMainMethod, List<String> entryMethodNames);
}
