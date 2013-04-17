package soot.jimple.infoflow.entryPointCreators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.G;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.VoidType;
import soot.dava.internal.javaRep.DIntConstant;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;

/**
 * Common base class for all entry point creators. Implementors must override the
 * createDummyMainInternal method to provide their entry point implementation.
 */
public abstract class BaseEntryPointCreator implements IEntryPointCreator {

	protected Map<String, Local> localVarsForClasses = new HashMap<String, Local>();
	private boolean substituteCallParams = false;
	private List<String> substituteClasses;
	
	public void setSubstituteCallParams(boolean b){
		substituteCallParams = b;
	}
	
	public void setSubstituteClasses(List<String> l){
		substituteClasses = l;
	}

	@Override
	public SootMethod createDummyMain(List<String> methods) {
		// Load the substitution classes
		if (substituteCallParams)
			for (String className : substituteClasses)
				Scene.v().forceResolve(className, SootClass.BODIES).setApplicationClass();
		
		return this.createDummyMainInternal(methods);
	}

	/**
	 * Implementors need to overwrite this method for providing the actual dummy
	 * main method
	 * @param methods The methods to be called inside the dummy main method
	 * @return The generated dummy main method
	 */
	protected abstract SootMethod createDummyMainInternal(List<String> methods);
	
	protected SootMethod createEmptyMainMethod(JimpleBody body){
		SootMethod mainMethod = new SootMethod("dummyMainMethod", new ArrayList<Type>(), VoidType.v());
		body.setMethod(mainMethod);
		mainMethod.setActiveBody(body);
		SootClass mainClass = new SootClass("dummyMainClass");
		mainClass.addMethod(mainMethod);
		// First add class to scene, then make it an application class
		// as addClass contains a call to "setLibraryClass" 
		Scene.v().addClass(mainClass);
		mainClass.setApplicationClass();
		return mainMethod;
	}
	
	protected Stmt buildMethodCall(SootMethod currentMethod, JimpleBody body, Local classLocal, LocalGenerator gen){
		assert currentMethod != null : "Current method was null";
		assert body != null : "Body was null";
		assert gen != null : "Local generator was null";
		
		Local stringLocal = null;
		InvokeExpr invokeExpr;
		if(currentMethod.getParameterCount()>0){
			List<Object> args = new LinkedList<Object>();
			for(Object ob :currentMethod.getParameterTypes()){
				if(ob instanceof Type){
					Type t = (Type)ob;
					SootClass classToType = Scene.v().getSootClass(t.toString());
					if(classToType != null){
						Value val = generateClassConstructor(classToType, body);
						if(val != null)
							args.add(val);
					}
				}
			}
			if(currentMethod.isStatic()){
				invokeExpr = Jimple.v().newStaticInvokeExpr(currentMethod.makeRef(), args);
			}else{
				assert classLocal != null : "Class local method was null for non-static method call";
				invokeExpr = Jimple.v().newVirtualInvokeExpr(classLocal, currentMethod.makeRef(),args);
			}
		}else{
			if(currentMethod.isStatic()){
				invokeExpr = Jimple.v().newStaticInvokeExpr(currentMethod.makeRef());
			}else{
				assert classLocal != null : "Class local method was null for non-static method call";
				invokeExpr = Jimple.v().newVirtualInvokeExpr(classLocal, currentMethod.makeRef());
			}
		}
		 
		Stmt stmt;
		if (!(currentMethod.getReturnType() instanceof VoidType)) {
			stringLocal = gen.generateLocal(currentMethod.getReturnType());
			stmt = Jimple.v().newAssignStmt(stringLocal, invokeExpr);
			
		} else {
			stmt = Jimple.v().newInvokeStmt(invokeExpr);
		}
		body.getUnits().add(stmt);
		return stmt;
	}
	
	protected Local generateClassConstructor(SootClass createdClass, JimpleBody body) {
		return this.generateClassConstructor(createdClass, body, new HashSet<SootClass>());
	}
	
	private Local generateClassConstructor(SootClass createdClass, JimpleBody body,
			Set<SootClass> constructionStack) {
		if (createdClass == null)
			return null;
		
		// We cannot create instances of phantom classes as we do not have any
		// constructor information for them
		if (createdClass.isPhantom() || createdClass.isPhantomClass()) {
			System.out.println("Cannot generate constructor for phantom class " + createdClass.getName());
			return null;
		}

		LocalGenerator generator = new LocalGenerator(body);

		// if sootClass is simpleClass:
		if (isSimpleType(createdClass.toString())) {
			Local varLocal =  generator.generateLocal(getSimpleTypeFromType(createdClass.getType()));
			
			AssignStmt aStmt = Jimple.v().newAssignStmt(varLocal, getSimpleDefaultValue(createdClass.toString()));
			body.getUnits().add(aStmt);
			return varLocal;
		}
		
		Local tempLocal = generator.generateLocal(RefType.v(createdClass));			
		boolean isInnerClass = createdClass.getName().contains("$");
		String outerClass = isInnerClass ? createdClass.getName().substring
				(0, createdClass.getName().lastIndexOf("$")) : "";
		
		// Make sure that we don't run into loops
		if (!constructionStack.add(createdClass)) {
			System.err.println("Ran into a constructor generation loop, aborting...");
			return null;
		}
		if(createdClass.isInterface() || createdClass.isAbstract()){
			if(substituteCallParams){
				// Find a matching implementor of the interface
				List<SootClass> classes;
				if (createdClass.isInterface())
					classes = Scene.v().getActiveHierarchy().getImplementersOf(createdClass);
				else
					classes = Scene.v().getActiveHierarchy().getSubclassesOf(createdClass);
				boolean foundOne = false;
				for(SootClass sClass : classes){
					if(substituteClasses.contains(sClass.toString())){
						AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal,
								generateClassConstructor(sClass, body, constructionStack));
						body.getUnits().add(assignStmt);
						foundOne = true;
					}
				}
				if(!foundOne){
					System.err.println("Warning, cannot create valid constructor for " + createdClass +
							", because it is " + (createdClass.isInterface() ? "an interface" :
								(createdClass.isAbstract() ? "abstract" : ""))+ " and cannot substitute with subclass");
					return null;
				}
				return tempLocal;
			}
			else{
				System.err.println("Warning, cannot create valid constructor for " + createdClass +
					", because it is " + (createdClass.isInterface() ? "an interface" :
						(createdClass.isAbstract() ? "abstract" : "")));
				//build the expression anyway:
				/* SA: why should we?
				NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(createdClass));
				AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, newExpr);
				body.getUnits().add(assignStmt);
				*/
				return null;
			}
		}
		else{
			// Build the "new" expression
			NewExpr newExpr = Jimple.v().newNewExpr(RefType.v(createdClass));
			AssignStmt assignStmt = Jimple.v().newAssignStmt(tempLocal, newExpr);
			body.getUnits().add(assignStmt);		
			
			// Find a constructor we can invoke
			boolean constructorFound = false;
			for (SootMethod currentMethod : createdClass.getMethods()) {
				if (currentMethod.isPrivate() || !currentMethod.isConstructor())
					continue;
				
				List<Type> typeList = (List<Type>) currentMethod.getParameterTypes();
				List<Object> params = new LinkedList<Object>();
				for (Type type : typeList) {
					String typeName = type.toString().replaceAll("\\[\\]]", "");
					if (isSimpleType(type.toString())) {
						Local primLoc = createPrimitiveLocal(generator, body, type);
						params.add(primLoc);
					}
					else if (isInnerClass && typeName.equals(outerClass)
							&& this.localVarsForClasses.containsKey(typeName))
						params.add(this.localVarsForClasses.get(typeName));
					else if (Scene.v().containsClass(typeName)) {
						SootClass typeClass = Scene.v().getSootClass(typeName);
						// 2. Type not public:
						if (!typeClass.isPrivate() && !typeClass.toString().equals(createdClass.toString()))
							params.add(generateClassConstructor(typeClass, body));
						else
							params.add(NullConstant.v());
					}
					else {
						System.out.println("Type not found: " + typeName + ", using java.lang.Object instead");
						params.add(generateClassConstructor(Scene.v().getSootClass("java.lang.Object"), body));
					}
				}

				// Create the constructor invocation
				VirtualInvokeExpr vInvokeExpr;
				if (params.isEmpty() || params.contains(null))
					vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, currentMethod.makeRef());
				else
					vInvokeExpr = Jimple.v().newVirtualInvokeExpr(tempLocal, currentMethod.makeRef(), params);

				// Make sure to store return values
				if (!(currentMethod.getReturnType() instanceof VoidType)) { 
					Local possibleReturn = generator.generateLocal(currentMethod.getReturnType());
					AssignStmt assignStmt2 = Jimple.v().newAssignStmt(possibleReturn, vInvokeExpr);
					body.getUnits().add(assignStmt2);
				}
				else
					body.getUnits().add(Jimple.v().newInvokeStmt(vInvokeExpr));
					
				constructorFound = true;
				break;
			}
			if (!constructorFound) {
				System.err.println("Could not find a suitable constructor for class "
						+ createdClass.getName());
				return null;
			}
			return tempLocal;
		}
	}

	private Local createPrimitiveLocal(LocalGenerator generator, JimpleBody body, Type type) {
		Local varLocal = generator.generateLocal(getSimpleTypeFromType(type));
		AssignStmt aStmt = Jimple.v().newAssignStmt(varLocal, getSimpleDefaultValue(type.toString()));
		body.getUnits().add(aStmt);
		return varLocal;
	}

	private Type getSimpleTypeFromType(Type type) {
		if (type.toString().equals("java.lang.String")) {
			assert type instanceof RefType;
			return RefType.v(((RefType) type).getSootClass());
		}
		if (type.toString().equals("void"))
			return soot.VoidType.v();
		if (type.toString().equals("char"))
			return soot.CharType.v();
		if (type.toString().equals("byte"))
			return soot.ByteType.v();
		if (type.toString().equals("short"))
			return soot.ShortType.v();
		if (type.toString().equals("int"))
			return soot.IntType.v();
		if (type.toString().equals("float"))
			return soot.FloatType.v();
		if (type.toString().equals("long"))
			return soot.LongType.v();
		if (type.toString().equals("double"))
			return soot.DoubleType.v();
		if (type.toString().equals("boolean"))
			return soot.BooleanType.v();
		throw new RuntimeException("Unknown simple type: " + type);
	}

	protected static boolean isSimpleType(String t) {
		if (t.equals("java.lang.String")
				|| t.equals("void")
				|| t.equals("char")
				|| t.equals("byte")
				|| t.equals("short")
				|| t.equals("int")
				|| t.equals("float")
				|| t.equals("long")
				|| t.equals("double")
				|| t.equals("boolean")) {
			return true;
		} else {
			return false;
		}
	}

	protected Value getSimpleDefaultValue(String t) {
		if (t.equals("java.lang.String"))
			return StringConstant.v("");
		if (t.equals("char"))
			return DIntConstant.v(0, CharType.v());
		if (t.equals("byte"))
			return DIntConstant.v(0, ByteType.v());
		if (t.equals("short"))
			return DIntConstant.v(0, ShortType.v());
		if (t.equals("int"))
			return IntConstant.v(0);
		if (t.equals("float"))
			return FloatConstant.v(0);
		if (t.equals("long"))
			return LongConstant.v(0);
		if (t.equals("double"))
			return DoubleConstant.v(0);
		if (t.equals("boolean"))
			return DIntConstant.v(0, BooleanType.v());

		//also for arrays etc.
		return G.v().soot_jimple_NullConstant();
	}

}
