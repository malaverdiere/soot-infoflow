package soot.jimple.infoflow.nativ;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.EquivalentValue;
import soot.PrimType;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;

public class DefaultNativeCallHandler extends NativeCallHandler {

	public Set<Abstraction> getTaintedValues(Stmt call, Abstraction source, List<Value> params){
		HashSet<Abstraction> set = new HashSet<Abstraction>();
		
		//check some evaluated methods:
		
		//arraycopy:
		//arraycopy(Object src, int srcPos, Object dest, int destPos, int length)
        //Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array.
		if(call.getInvokeExpr().getMethod().toString().contains("arraycopy")){
			if(params.get(0).equals(source.getAccessPath().getPlainValue())){
				set.add(new Abstraction(new EquivalentValue(params.get(2)), source.getSource()));
			}
		}else{
			//generic case: add taint to all non-primitive datatypes:
			for (int i = 0; i < params.size(); i++) {
				Value argValue = params.get(i);
				if (!(argValue.getType() instanceof PrimType)) {
					set.add(new Abstraction(new EquivalentValue(argValue), source.getSource()));
				}
			}	
		}
		//add the  returnvalue:
		if(call instanceof DefinitionStmt){
			DefinitionStmt dStmt = (DefinitionStmt) call;
			set.add(new Abstraction(new EquivalentValue(dStmt.getLeftOp()), source.getSource()));
		}
		
		return set;
	}
}
