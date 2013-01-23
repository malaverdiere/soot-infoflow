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
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
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
	/**
	 * this method solves the problem that a field gets tainted inside a method which is assigned before, e.g.:
	 * 1 a = x
	 * 2 x.f = taintedValue
	 * 3 return a.f 
	 * --> return value must be tainted
	 * @param units the units of the method
	 * @param stopUnit the unit in which the taint is happening (line 2 in example) - we do not have to look further
	 * @param base the base value which gets tainted (x in example)
	 * @param instanceField the field which gets tainted (f in example)
	 * @return set of aliases (a.f in example)
	 */
	protected Set<Value> getAliasesinMethod(PatchingChain<Unit> units, Unit stopUnit, Value base, SootFieldRef instanceField){
		HashSet<Value> val = new HashSet<Value>();
		for(Unit u : units){
			if(u.equals(stopUnit)){
				return val;
			}
			if(u instanceof AssignStmt){
				AssignStmt aStmt = (AssignStmt) u;
				if(aStmt.getLeftOp().toString().equals(base.toString()) && aStmt.getRightOp() != null){
					//create new alias
					if(aStmt.getRightOp() instanceof Local){ //otherwise no fieldRef possible (and therefore cannot be referenced)
						if(instanceField != null){
							JInstanceFieldRef newRef = new JInstanceFieldRef(aStmt.getRightOp(), instanceField);
							val.add(newRef);
						}else{
							val.add(aStmt.getRightOp());
						}
					}else if(aStmt.getRightOp() instanceof InstanceFieldRef || aStmt.getRightOp() instanceof StaticFieldRef){
						//because of max(length(accesspath)) = 1 we can only taint whole instancefield:
						val.add(aStmt.getRightOp());
					}
					val.addAll(getAliasesinMethod(units, u, aStmt.getRightOp(), instanceField));
				}
				if(aStmt.getRightOp().toString().equals(base.toString()) && aStmt.getLeftOp() != null){
					if(aStmt.getLeftOp() instanceof Local){ //otherwise no fieldRef possible (and therefore cannot be referenced)	
						if(instanceField != null){
							JInstanceFieldRef newRef = new JInstanceFieldRef(aStmt.getLeftOp(), instanceField);
							val.add(newRef);
						}else{
							val.add(aStmt.getLeftOp());
						}
						
					}else if(aStmt.getLeftOp() instanceof InstanceFieldRef || aStmt.getLeftOp() instanceof StaticFieldRef){
						//because of max(length(accesspath)) = 1 we can only taint whole instancefield:
						val.add(aStmt.getLeftOp());
					}
					val.addAll(getAliasesinMethod(units, u, aStmt.getLeftOp(), instanceField));
				}
			}
		}
		return val;
	}
	
	protected static String getStaticFieldRefStringRepresentation(StaticFieldRef ref){
		return ref.getFieldRef().declaringClass().getName() + "."+ref.getFieldRef().name();
	}

}
