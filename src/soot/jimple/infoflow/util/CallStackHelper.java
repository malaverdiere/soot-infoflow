package soot.jimple.infoflow.util;

import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

public class CallStackHelper {
	
	/**
	 * this method checks if a callSite is equal to another callSite by 
	 * comparing the methods called by their invokeExpr (if available)
	 * this is necessary for callStack-Handling (from an analysis of a method
	 * we want to return to the statement which called the method -> valid path) 
	 * @param callSite1
	 * @param callSite2
	 * @return
	 */
	public static boolean isEqualCall(Unit callSite1, Unit callSite2){
		if(callSite1 instanceof Stmt && callSite2 instanceof Stmt){
			Stmt s1 = (Stmt) callSite1;
			Stmt s2 = (Stmt) callSite2;
			
			if(s1.containsInvokeExpr() && s2.containsInvokeExpr()){
				InvokeExpr ie1 = s1.getInvokeExpr();
				InvokeExpr ie2 = s2.getInvokeExpr();
				
				if(ie1.getMethod().equals(ie2.getMethod())){
					return true;
				}
				
				
			}
			
		}
		return callSite1.equals(callSite2);
	}

}
