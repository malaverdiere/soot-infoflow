package soot.jimple.infoflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import heros.InterproceduralCFG;
import soot.Local;
import soot.PatchingChain;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.toolkits.ide.DefaultJimpleIFDSTabulationProblem;

public abstract class AbstractInfoflowProblem extends DefaultJimpleIFDSTabulationProblem<Abstraction, InterproceduralCFG<Unit, SootMethod>> {

	final Set<Unit> initialSeeds = new HashSet<Unit>();
	final HashMap<String, List<String>> results;
	
	public AbstractInfoflowProblem(InterproceduralCFG<Unit, SootMethod> icfg) {
		super(icfg);
		results = new HashMap<String, List<String>>();
	}
	
	protected Set<Value> getAliasesinMethod(PatchingChain<Unit> units, Unit stopUnit, Value base, SootFieldRef instanceField){
		HashSet<Value> val = new HashSet<Value>();
		for(Unit u : units){
			if(u.equals(stopUnit)){
				return val;
			}
			if(u instanceof AssignStmt){ //TODO: hier ebenfalls checken ob nicht ifstmt (wie oben)
				AssignStmt aStmt = (AssignStmt) u;
				if(aStmt.getLeftOp().toString().equals(base.toString()) && aStmt.getRightOp() != null){
					//create new alias
					if(aStmt.getRightOp() instanceof Local){ //otherwise no fieldRef possible (and therefore cannot be referenced)
						JInstanceFieldRef newRef = new JInstanceFieldRef(aStmt.getRightOp(), instanceField);
						val.add(newRef);
					}
					val.addAll(getAliasesinMethod(units, u, aStmt.getRightOp(), instanceField));
				} //not nice - change this - do not use toString (although it should be valid because we are only looking inside one method and are looking for the same object)
				if(aStmt.getRightOp().toString().equals(base.toString()) && aStmt.getLeftOp() != null){
					if(aStmt.getLeftOp() instanceof Local){ //otherwise no fieldRef possible (and therefore cannot be referenced)
						JInstanceFieldRef newRef = new JInstanceFieldRef(aStmt.getLeftOp(), instanceField);
						val.add(newRef);
					}
					val.addAll(getAliasesinMethod(units, u, aStmt.getLeftOp(), instanceField));
				}
			}
		}
		return val;
	}

}
