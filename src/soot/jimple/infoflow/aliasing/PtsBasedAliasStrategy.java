package soot.jimple.infoflow.aliasing;

import heros.solver.PathEdge;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.PointsToSet;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.heros.InfoflowCFG;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * A simple points-to-based aliasing strategy for FlowDroid
 * 
 * @author Steven Arzt
 */
public class PtsBasedAliasStrategy extends AbstractAliasStrategy {
	
	private final Table<SootMethod, Abstraction, Boolean> aliases = HashBasedTable.create();

	public PtsBasedAliasStrategy(InfoflowCFG cfg) {
		super(cfg);
	}

	@Override
	public void computeAliasTaints(Abstraction d1, Stmt src, Value targetValue,
			Set<Abstraction> taintSet, SootMethod method, Abstraction newAbs) {
		computeAliasTaintsInternal(d1, method, newAbs, Collections.<SootField>emptyList());
	}

	public void computeAliasTaintsInternal(Abstraction d1, SootMethod method,
			Abstraction newAbs, List<SootField> appendFields) {
		if (aliases.contains(method, newAbs))
			return;
		aliases.put(method, newAbs, Boolean.TRUE);
		
		if (method.getName().equals("methodTest1"))
			System.out.println("x");
		
		PointsToSet ptsTaint = getPointsToSet(newAbs.getAccessPath());
		SootField[] appendFieldsA = appendFields.toArray(new SootField[appendFields.size()]);
		
		// We run once per method and we are flow-insensitive anyway, so we
		// can just say that every use of a variable aliased with a tainted
		// one automatically taints the corresponding def set.
		for (Unit u : method.getActiveBody().getUnits()) {			
			Stmt stmt = (Stmt) u;
			if (stmt.containsInvokeExpr()) {
				// If we have a call and the base object aliases with our base
				// object, we also need to look for aliases in the callee
				InvokeExpr invExpr = (InvokeExpr) stmt.getInvokeExpr();
				if (invExpr instanceof InstanceInvokeExpr && !newAbs.getAccessPath().isStaticFieldRef()) {
					InstanceInvokeExpr iinvExpr = (InstanceInvokeExpr) invExpr;
					PointsToSet ptsBase = getPointsToSet((Local) iinvExpr.getBase());
					PointsToSet ptsBaseOrg = getPointsToSet(newAbs.getAccessPath().getPlainLocal());
					if (ptsBase.hasNonEmptyIntersection(ptsBaseOrg))
						System.out.println("x");
				}
			}
			else if (u instanceof DefinitionStmt) {
				DefinitionStmt assign = (DefinitionStmt) u;
				
				// If we have a = b and our taint is an alias to b, we must add
				// a taint for a.
				if (assign.getRightOp() instanceof FieldRef || assign.getRightOp() instanceof Local
						|| assign.getRightOp() instanceof ArrayRef)
					if (isAliasedAtStmt(ptsTaint, assign.getRightOp())) {
						Abstraction aliasAbsLeft = newAbs.deriveNewAbstraction(new AccessPath
								(assign.getLeftOp(), appendFieldsA), stmt);
						processAliasEdge(d1, method, newAbs, appendFields, stmt, aliasAbsLeft);
					}

				// If we have a = b and our taint is an alias to a, we must add
				// a taint for b.
				if (assign.getLeftOp() instanceof FieldRef || assign.getLeftOp() instanceof Local
						|| assign.getLeftOp() instanceof ArrayRef)
					if (assign.getRightOp() instanceof FieldRef || assign.getRightOp() instanceof Local
							|| assign.getRightOp() instanceof ArrayRef)
						if (isAliasedAtStmt(ptsTaint, assign.getLeftOp())) {
							Abstraction aliasAbsRight = newAbs.deriveNewAbstraction(new AccessPath
									(assign.getRightOp(), appendFieldsA), stmt);
							processAliasEdge(d1, method, newAbs, appendFields, stmt, aliasAbsRight);
						}
			}
		}
	}

	private void processAliasEdge(Abstraction d1, SootMethod method, Abstraction originalAbs,
			List<SootField> appendFields, Stmt stmt, Abstraction aliasAbs) {
		getForwardSolver().processEdge(new PathEdge<Unit, Abstraction>(d1, stmt, aliasAbs));
		
		// Also check for aliases for parts of the access path
		final AccessPath ap = originalAbs.getAccessPath();
		if ((ap.isInstanceFieldRef() && ap.getFirstField() != null)
				|| (ap.isStaticFieldRef() && ap.getFieldCount() > 1)){
			List<SootField> appendList = new LinkedList<SootField>(appendFields);
			appendList.add(originalAbs.getAccessPath().getFirstField());
			computeAliasTaintsInternal(d1, method, originalAbs.deriveNewAbstraction
					(originalAbs.getAccessPath().dropLastField(), stmt), appendList);
		}
	}
	
	private boolean isAliasedAtStmt(PointsToSet ptsTaint, Value val) {
		PointsToSet ptsRight = getPointsToSet(val);
		return ptsTaint.hasNonEmptyIntersection(ptsRight);
	}
	
	/**
	 * Gets the points-to-set for the given value
	 * @param targetValue The value for which to get the points-to-set
	 * @return The points-to-set for the given value
	 */
	private PointsToSet getPointsToSet(Value targetValue) {
		if (targetValue instanceof Local)
			return Scene.v().getPointsToAnalysis().reachingObjects((Local) targetValue);
		else if (targetValue instanceof InstanceFieldRef) {
			InstanceFieldRef iref = (InstanceFieldRef) targetValue;
			return Scene.v().getPointsToAnalysis().reachingObjects((Local) iref.getBase(), iref.getField());
		}
		else if (targetValue instanceof StaticFieldRef) {
			StaticFieldRef sref = (StaticFieldRef) targetValue;
			return Scene.v().getPointsToAnalysis().reachingObjects(sref.getField());
		}
		else if (targetValue instanceof ArrayRef) {
			ArrayRef aref = (ArrayRef) targetValue;
			return Scene.v().getPointsToAnalysis().reachingObjects((Local) aref.getBase());
		}
		else
			throw new RuntimeException("Unexpected value type for aliasing: " + targetValue.getClass());
	}

	/**
	 * Gets the points-to-set for the given access path
	 * @param accessPath The access path for which to get the points-to-set
	 * @return The points-to-set for the given access path
	 */
	private PointsToSet getPointsToSet(AccessPath accessPath) {
		if (accessPath.isLocal())
			return Scene.v().getPointsToAnalysis().reachingObjects(accessPath.getPlainLocal());
		else if (accessPath.isInstanceFieldRef())
			return Scene.v().getPointsToAnalysis().reachingObjects(accessPath.getPlainLocal(), accessPath.getFirstField());
		else if (accessPath.isStaticFieldRef())
			return Scene.v().getPointsToAnalysis().reachingObjects(accessPath.getFirstField());
		else
			throw new RuntimeException("Unexepected access path type");
	}
	
}
