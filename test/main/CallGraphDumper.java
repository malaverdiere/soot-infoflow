/**
 * taken from http://www.sable.mcgill.ca/pipermail/soot-list/2004-October/000047.html
 * and adjusted to work without main method
 */

package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

	public class CallGraphDumper extends SceneTransformer {
	    public static void main(String[] args) 
	    {
		PackManager.v().getPack("wjtp").add(
		    new Transform("wjtp.dumpcg", new CallGraphDumper()));

		//soot.Main.main(args);
		Options.v().parse(args);
		SootClass c = Scene.v().forceResolve("soot.jimple.infoflow.test.TestNoMain", SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		c.setApplicationClass();
		List<SootMethod> entryPoints = new ArrayList<SootMethod>();
		entryPoints.add(c.getMethodByName("onChange"));
		entryPoints.add(c.getMethodByName("function1"));
		entryPoints.add(c.getMethodByName("onCreate"));
		Scene.v().setEntryPoints(entryPoints);
		PackManager.v().runPacks();
	    }

	    protected void internalTransform(String phaseName, Map options)
	    {
	        CallGraph cg = Scene.v().getCallGraph();

	        Iterator it = cg.listener();
	        while( it.hasNext() ) {
	            soot.jimple.toolkits.callgraph.Edge e =
	                (soot.jimple.toolkits.callgraph.Edge) it.next();
	            System.out.println(""+e.src()+e.srcStmt()+" ="+e.kind()+"=> "+e.tgt());
	        }
	    }
	}

