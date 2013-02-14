package soot.jimple.infoflow.data;

import java.util.HashMap;

import soot.EquivalentValue;
import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.jimple.InstanceFieldRef;
import soot.jimple.spark.sets.PointsToSetInternal;

public class Abstraction {
	private final AccessPath accessPath;
	private final EquivalentValue source;
	// field: key = subsignature, value = corresponding pts!
	private final HashMap<String, PointsToSetInternal> heapmap;
	

	public Abstraction(EquivalentValue taint, EquivalentValue src){
		this(new AccessPath(taint), src);
	}
	
	public Abstraction(AccessPath p, EquivalentValue src){
		source = src;
		accessPath = p;
		heapmap = new HashMap<String, PointsToSetInternal>();
		if (p.isInstanceFieldRef()) {
			PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
			Local l = (Local) p.getPlainValue();
			SootClass sc = Scene.v().getSootClass(l.getType().toString());
			if (sc.declaresFieldByName(p.getField())) {
				// PointsToSet pts = pta.reachingObjects((Local) p.getPlainValue(), sc.getFieldByName(p.getField()));
				PointsToSet pts = pta.reachingObjects((Local) p.getPlainValue());
				// TODO: change this:
				if (pts instanceof PointsToSetInternal) {
					heapmap.put(sc.getFieldByName(p.getField()).getSubSignature(), (PointsToSetInternal) pts);
				}
			}
		}
	}
	
	public EquivalentValue getSource() {
		return source;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Abstraction other = (Abstraction) obj;
		if (accessPath == null) {
			if (other.accessPath != null)
				return false;
		} else if (!accessPath.equals(other.accessPath))
			return false;
		if (heapmap == null) {
			if (other.heapmap != null)
				return false;
		} else if (!heapmap.equals(other.heapmap))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessPath == null) ? 0 : accessPath.hashCode());
		result = prime * result + ((heapmap == null) ? 0 : heapmap.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}
	
	@Override
	public String toString(){
		if(accessPath != null && source != null){
			return accessPath.toString() + " /source: "+ source.toString();
		}
		if(accessPath != null){
			return accessPath.toString();
		}
		return "Abstraction (null)";
	}
	
	public AccessPath getAccessPath(){
		return accessPath;
	}

	public HashMap<String, PointsToSetInternal> getHeapMap() {
		return heapmap;
	}
	
	public Abstraction copyHeapMap() {
		Abstraction abs = new Abstraction(new AccessPath(null, null), source);
		abs.heapmap.putAll(this.heapmap);
		return abs;
	}

	public boolean checkHeapMap(InstanceFieldRef ref) {
		// performance: only compare if there is only the heapmap left (which means we can't compare by accessPath)
		if (accessPath.getField() == null && accessPath.getPlainValue() == null && heapmap.containsKey(ref.getField().getSubSignature())) {
			PointsToSetInternal pts = heapmap.get(ref.getField().getSubSignature());
			PointsToAnalysis pta = Scene.v().getPointsToAnalysis();

			PointsToSet ptsNewRaw = pta.reachingObjects((Local) ref.getBase());
			// TODO: that does not work this way!
			// PointsToSet ptsNewRaw = pta.reachingObjects((Local) ref.getBase(), ref.getField());
			if (ptsNewRaw instanceof PointsToSetInternal) {
				PointsToSetInternal ptsNew = (PointsToSetInternal) ptsNewRaw;
				if (pts.hasNonEmptyIntersection(ptsNew)) {
					return true;
				}
			}else{
				System.out.println("no ptsInternal: " + ptsNewRaw.getClass() +" created for " + ref);
			}
		}

		return false;
	}

	public boolean hasNewHeapElements() {
		return !heapmap.isEmpty() && (accessPath.getField() != null || accessPath.getPlainValue() != null);
	}
	
	public boolean hasOnlyHeapElements() {
		return !heapmap.isEmpty() && accessPath.getField() == null && accessPath.getPlainValue() == null;
	}

	
}
