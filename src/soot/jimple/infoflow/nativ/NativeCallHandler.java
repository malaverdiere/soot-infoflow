package soot.jimple.infoflow.nativ;

import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;

public abstract class NativeCallHandler {

	
	public abstract Set<Abstraction> getTaintedValues(Stmt call, Abstraction source, List<Value> params);
}
