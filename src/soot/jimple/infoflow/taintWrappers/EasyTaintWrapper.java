/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow.taintWrappers;

import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;

/**
 * A list of methods is passed which contains signatures of instance methods
 * that taint their base objects if they are called with a tainted parameter.
 * When a base object is tainted, all return values are tainted, too.
 * For static methods, only the return value is assumed to be tainted when
 * the method is called with a tainted parameter value.
 * 
 * @author Christian Fritz, Steven Arzt
 *
 */
public class EasyTaintWrapper extends AbstractTaintWrapper implements Cloneable {
    private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<String, Set<String>> classList;
	private final Map<String, Set<String>> excludeList;
	private final Map<String, Set<String>> killList;
	private final Set<String> includeList;
	
	private boolean aggressiveMode = false;
	private boolean alwaysModelEqualsHashCode = true;
	
	public EasyTaintWrapper(Map<String, Set<String>> classList){
		this(classList, new HashMap<String, Set<String>>(),
				new HashMap<String, Set<String>>(),
				new HashSet<String>());
	}
	
	public EasyTaintWrapper(Map<String, Set<String>> classList,
			Map<String, Set<String>> excludeList) {
		this(classList, excludeList, new HashMap<String, Set<String>>(),
				new HashSet<String>());
	}

	public EasyTaintWrapper(Map<String, Set<String>> classList,
			Map<String, Set<String>> excludeList,
			Map<String, Set<String>> killList) {
		this(classList, excludeList, killList, new HashSet<String>());
	}

	public EasyTaintWrapper(Map<String, Set<String>> classList,
			Map<String, Set<String>> excludeList,
			Map<String, Set<String>> killList,
			Set<String> includeList) {
		this.classList = classList;
		this.excludeList = excludeList;
		this.killList = killList;
		this.includeList = includeList;
	}

	public EasyTaintWrapper(String f) throws IOException{
        this(new File(f));
    }

	public EasyTaintWrapper(File f) throws IOException{
        this(new BufferedReader(new FileReader(f)));
	}

    public EasyTaintWrapper(InputStream is) throws IOException {
        this(new BufferedReader(new InputStreamReader(is)));
    }

    private EasyTaintWrapper(BufferedReader reader) throws IOException {
        try {
        this.includeList = new HashSet<String>();
        String line = reader.readLine();
        List<String> methodList = new LinkedList<String>();
        List<String> excludeList = new LinkedList<String>();
        List<String> killList = new LinkedList<String>();
        while(line != null){
            if (!line.isEmpty() && !line.startsWith("%"))
                if (line.startsWith("~"))
                    excludeList.add(line.substring(1));
                else if (line.startsWith("-"))
                    killList.add(line.substring(1));
                else if (line.startsWith("^"))
                    includeList.add(line.substring(1));
                else
                    methodList.add(line);
            line = reader.readLine();
        }
        this.classList = SootMethodRepresentationParser.v().parseClassNames(methodList, true);
        this.excludeList = SootMethodRepresentationParser.v().parseClassNames(excludeList, true);
        this.killList = SootMethodRepresentationParser.v().parseClassNames(killList, true);
        logger.info("Loaded wrapper entries for {} classes and {} exclusions.", classList.size(), excludeList.size());
        } finally{
            reader.close();
        }
    }

	public EasyTaintWrapper(EasyTaintWrapper taintWrapper) {
		this(taintWrapper.classList, taintWrapper.excludeList, taintWrapper.killList, taintWrapper.includeList);
	}
	
    @Override
	public Set<AccessPath> getTaintsForMethod(Stmt stmt, AccessPath taintedPath) {
		if (!stmt.containsInvokeExpr())
			return Collections.emptySet();
		
		Set<AccessPath> taints = new HashSet<AccessPath>();

		SootMethod method = stmt.getInvokeExpr().getMethod();
		List<String> methodList = getMethodsForClass(method.getDeclaringClass());
		
		// If the callee is a phantom class or has no body, we pass on the taint
		if (method.isPhantom() || !method.hasActiveBody())
			taints.add(taintedPath);

		// For the moment, we don't implement static taints on wrappers. Pass it on
		// not to break anything
		if(taintedPath.isStaticFieldRef())
			return Collections.singleton(taintedPath);

		// For implicit flows, we always taint the return value and the base
		// object on the empty abstraction.
		if (taintedPath.isEmpty()) {
			taints.add(taintedPath);
			if (stmt instanceof DefinitionStmt)
				taints.add(new AccessPath(((DefinitionStmt) stmt).getLeftOp(), true));
			if (stmt.containsInvokeExpr())
				if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr)
					taints.add(new AccessPath(((InstanceInvokeExpr) stmt.getInvokeExpr()).getBase(), true));
			return taints;
		}
		
		// Do we handle equals() and hashCode() separately?
		boolean taintEqualsHashCode = alwaysModelEqualsHashCode
				&& (method.getSubSignature().equals("boolean equals(java.lang.Object)")
						|| method.getSubSignature().equals("int hashCode()"));
		
		// If this is not one of the supported classes, we skip it
		boolean isSupported = false;
		for (String supportedClass : this.includeList)
			if (method.getDeclaringClass().getName().startsWith(supportedClass)) {
				isSupported = true;
				break;
			}
		if (!isSupported && !aggressiveMode && !taintEqualsHashCode)
			return taints;
		
		if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
			InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();			
			if (iiExpr.getBase().equals(taintedPath.getPlainValue())) {
				// If the base object is tainted, we have to check whether we must kill the taint
				Set<String> killMethods = this.killList.get(stmt.getInvokeExpr().getMethod().getDeclaringClass().getName());
				if (killMethods != null && killMethods.contains(stmt.getInvokeExpr().getMethod().getSubSignature()))
					return Collections.emptySet();

				// If the base object is tainted, all calls to its methods always return
				// tainted values
				if (stmt instanceof DefinitionStmt) {
					DefinitionStmt def = (DefinitionStmt) stmt;

					// Check for exclusions
					Set<String> excludedMethods = this.excludeList.get(def.getInvokeExpr().getMethod().getDeclaringClass().getName());
					if (excludedMethods == null || !excludedMethods.contains
							(def.getInvokeExpr().getMethod().getSubSignature()))
						taints.add(new AccessPath(def.getLeftOp(), true));
				}

				// If the base object is tainted, we pass this taint on
				taints.add(taintedPath);
			}
		}
				
		//if param is tainted && classList contains classname && if list. contains signature of method -> add propagation
		if ((isSupported || taintEqualsHashCode) && methodList.contains(method.getSubSignature()))
			for (Value param : stmt.getInvokeExpr().getArgs()) {
				if (param.equals(taintedPath.getPlainValue())) {
					// If we call a method on an instance with a tainted parameter, this
					// instance (base object) is assumed to be tainted.
					if (!taintEqualsHashCode)
						if (stmt.getInvokeExprBox().getValue() instanceof InstanceInvokeExpr)
							taints.add(new AccessPath(((InstanceInvokeExpr) stmt.getInvokeExprBox().getValue()).getBase(), true));
					
					// If make sure to also taint the left side of an assignment
					// if the object just got tainted 
					if (stmt instanceof DefinitionStmt)
						taints.add(new AccessPath(((DefinitionStmt) stmt).getLeftOp(), true));
				}
					
				// The parameter as such stays tainted
				taints.add(taintedPath);
			}
		
		return taints;
	}
	
	private List<String> getMethodsForClass(SootClass c){
		assert c != null;
		
		List<String> methodList = new LinkedList<String>();
		if(classList.containsKey(c.getName())){
			methodList.addAll(classList.get(c.getName()));
		}
		
		if(!c.isInterface()) {
			// We have to walk up the hierarchy to also include all methods
			// registered for superclasses
			Collection<SootClass> superclasses = Scene.v().getActiveHierarchy().getSuperclassesOf(c);
			for(SootClass sclass : superclasses){
				if(classList.containsKey(sclass.getName()))
					methodList.addAll(getMethodsForClass(sclass));
			}
		}
		
		// If we implement interfaces, we also need to check whether they in
		// turn are in our method list
		for (SootClass ifc : c.getInterfaces())
			for (SootClass pifc : Scene.v().getActiveHierarchy().getSuperinterfacesOfIncluding(ifc))
				methodList.addAll(getMethodsForClass(pifc));
		
		return methodList;
	}

	private boolean hasMethodsForClass(SootMethod m) {
		if (classList.containsKey(m.getDeclaringClass().getName())
				|| excludeList.containsKey(m.getDeclaringClass().getName())
				|| killList.containsKey(m.getDeclaringClass().getName()))
			return true;
		
		if (!m.getDeclaringClass().isInterface()) {
			// We have to walk up the hierarchy to also include all methods
			// registered for superclasses
			Collection<SootClass> superclasses = Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(m.getDeclaringClass());
			for(SootClass sclass : superclasses) {
				if ((classList.containsKey(sclass.getName()) && classList.get(sclass.getName()).contains(m.getSubSignature()))
						|| excludeList.containsKey(sclass.getName()) && excludeList.get(sclass.getName()).contains(m.getSubSignature())
						|| killList.containsKey(sclass.getName()) && killList.get(sclass.getName()).contains(m.getSubSignature()))
					return true;

				for (SootClass ifc : sclass.getInterfaces())
					for (SootClass pifc : Scene.v().getActiveHierarchy().getSuperinterfacesOfIncluding(ifc))
						if ((classList.containsKey(pifc.getName()) && classList.get(pifc.getName()).contains(m.getSubSignature()))
								|| excludeList.containsKey(pifc.getName()) && excludeList.get(pifc.getName()).contains(m.getSubSignature())
								|| killList.containsKey(pifc.getName()) && killList.get(pifc.getName()).contains(m.getSubSignature()))
							return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean isExclusiveInternal(Stmt stmt, AccessPath taintedPath) {
		SootMethod method = stmt.getInvokeExpr().getMethod();
		
		// In aggressive mode, we always taint the return value if the base
		// object is tainted.
		if (aggressiveMode && stmt.getInvokeExpr() instanceof InstanceInvokeExpr) {
			InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();			
			if (iiExpr.getBase().equals(taintedPath.getPlainValue()))
				return true;
		}
		
		// Do we always model equals() and hashCode()?
		final String methodSubSig = stmt.getInvokeExpr().getMethod().getSubSignature();
		if (alwaysModelEqualsHashCode
				&& (methodSubSig.equals("boolean equals(java.lang.Object)") || methodSubSig.equals("int hashCode()")))
			return true;
		
		return hasMethodsForClass(method);
	}
	
	/**
	 * Sets whether the taint wrapper shall always assume the return value of a
	 * call "a = x.foo()" to be tainted if the base object is tainted, even if
	 * the respective method is not in the data file.
	 * @param aggressiveMode True if return values shall always be tainted if
	 * the base object on which the method is invoked is tainted, otherwise
	 * false
	 */
	public void setAggressiveMode(boolean aggressiveMode) {
		this.aggressiveMode = aggressiveMode;
	}
	
	/**
	 * Gets whether the taint wrapper shall always consider return values as
	 * tainted if the base object of the respective invocation is tainted
	 * @return True if return values shall always be tainted if the base
	 * object on which the method is invoked is tainted, otherwise false
	 */
	public boolean getAggressiveMode() {
		return this.aggressiveMode;
	}
	
	/**
	 * Sets whether the equals() and hashCode() methods shall always be modeled,
	 * regardless of the target type.
	 * @param alwaysModelEqualsHashCode True if the equals() and hashCode()
	 * methods shall always be modeled, regardless of the target type, otherwise
	 * false
	 */
	public void setAlwaysModelEqualsHashCode(boolean alwaysModelEqualsHashCode) {
		this.alwaysModelEqualsHashCode = alwaysModelEqualsHashCode;
	}
	
	/**
	 * Gets whether the equals() and hashCode() methods shall always be modeled,
	 * regardless of the target type.
	 * @return True if the equals() and hashCode() methods shall always be modeled,
	 * regardless of the target type, otherwise false
	 */
	public boolean getAlwaysModelEqualsHashCode() {
		return this.alwaysModelEqualsHashCode;
	}
	
	/**
	 * Registers a prefix of class names to be included when generating taints.
	 * All classes whose names don't start with a registered prefix will be
	 * skipped.
	 * @param prefix The prefix to register
	 */
	public void addIncludePrefix(String prefix) {
		this.includeList.add(prefix);
	}
	
	/**
	 * Adds a method to which the taint wrapping rules shall apply
	 * @param className The class containing the method to be wrapped
	 * @param subSignature The subsignature of the method to be wrapped
	 */
	public void addMethodForWrapping(String className, String subSignature) {
		Set<String> methods = this.classList.get(className);
		if (methods == null) {
			methods = new HashSet<String>();
			this.classList.put(className, methods);
		}
		methods.add(subSignature);
	}
	
	@Override
	public EasyTaintWrapper clone() {
		return new EasyTaintWrapper(this);
	}

}
