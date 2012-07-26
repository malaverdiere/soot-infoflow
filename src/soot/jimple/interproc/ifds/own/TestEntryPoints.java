package soot.jimple.interproc.ifds.own;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.EntryPoints;
import soot.Scene;
import soot.SootClass;
import soot.Singletons.Global;
import soot.SootMethod;


public class TestEntryPoints extends EntryPoints {

	public TestEntryPoints(Global g) {
		super(g);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<SootMethod> all(){
		 List<SootMethod> ret = new ArrayList<SootMethod>();
	     ret.addAll( application() );
	     Iterator<SootClass> classIterator = Scene.v().getClasses().iterator();
			while(classIterator.hasNext()){ //TODO: isEmpty!!
				SootClass current = classIterator.next();
				System.out.println(current.getName() + " has the following methods:");
				for(SootMethod method : current.getMethods()){
					System.out.println(method.getName());
				}
			}
	     ret.addAll( implicit() );
	     return ret;
	}

}
