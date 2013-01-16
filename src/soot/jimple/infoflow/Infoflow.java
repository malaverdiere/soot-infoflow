package soot.jimple.infoflow;

import heros.InterproceduralCFG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.util.AndroidEntryPointCreator;
import soot.jimple.infoflow.util.IEntryPointCreator;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.toolkits.ide.JimpleIFDSSolver;
import soot.options.Options;

public class Infoflow implements IInfoflow {
	
	public static boolean DEBUG = true;
	public boolean local = false;
	public HashMap<String, List<String>> results;

	@Override
	public void computeInfoflow(String path, List<String> entryPoints, List<String> sources, List<String> sinks) {
		results = null;
		if(sources == null || sources.isEmpty()){
			System.out.println("Error: sources are empty!");
			return;
		}
		if(sinks == null || sinks.isEmpty()){
			if(sinks == null){
				sinks = new ArrayList<String>();
			}
			System.out.println("Warning: sinks are empty!");
		}
		
		//reset Soot:
		soot.G.reset();
		
		// convert to internal format:
		SootMethodRepresentationParser parser = new SootMethodRepresentationParser();
		// parse classNames as String and methodNames as string in soot representation
		HashMap<String, List<String>> classes = parser.parseClassNames(entryPoints);

		// add SceneTransformer which calculates and prints infoflow
		addSceneTransformer(sources, sinks);
		
		// prepare soot arguments:
//		ArgBuilder builder = new ArgBuilder();
		//String[] args = builder.buildArgs(path, classes.entrySet().iterator().next().getKey()); 
			
		// explicitly include packages for shorter runtime:
		List<String> includeList = new LinkedList<String>();
		includeList.add("java.lang.");
		includeList.add("java.util.");
		includeList.add("java.io.");
		includeList.add("sun.misc.");
		includeList.add("android.");
		includeList.add("org.apache.http.");
		includeList.add("de.test.");
		includeList.add("soot.");
		includeList.add("com.example.");
		includeList.add("com.jakobkontor.");
		includeList.add("java.net.");
		Options.v().set_include(includeList);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_whole_program(true);
		Options.v().set_soot_classpath(path);
		soot.options.Options.v().set_prepend_classpath(true);
		Options.v().set_process_dir(Arrays.asList(classes.keySet().toArray()));
		soot.options.Options.v().setPhaseOption("cg.spark","on");
		soot.options.Options.v().setPhaseOption("jb","use-original-names:true");
		//do not merge variables (causes problems with PointsToSets)
		soot.options.Options.v().setPhaseOption("jb.ulp","off");
		
		//Options.v().parse(args);
		//load all entryPoint classes with their bodies
		Scene.v().loadNecessaryClasses();
		for (Entry<String, List<String>> classEntry : classes.entrySet()) {
			SootClass c = Scene.v().forceResolve(classEntry.getKey(), SootClass.BODIES);
			c.setApplicationClass();
			if(DEBUG){
				for(String methodSignature : classEntry.getValue()){
					if (Scene.v().containsMethod(methodSignature)) {
						SootMethod method = Scene.v().getMethod(methodSignature);
						System.err.println(method.retrieveActiveBody().toString());
					}
				}
			}
		}
		// entryPoints are the entryPoints required by Soot to calculate Graph - if there is no main method, 
		//we have to create a new main method and use it as entryPoint and store our real entryPoints
		IEntryPointCreator epCreator = new AndroidEntryPointCreator();
		List<SootMethod> entrys = new LinkedList<SootMethod>();
		
		entrys.add(epCreator.createDummyMain(classes));
		Scene.v().setEntryPoints(entrys);
		PackManager.v().runPacks();
	}	

	public void addSceneTransformer(final List<String> sources, final List<String> sinks) {
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {

				AbstractInfoflowProblem problem;
				
				if(local){
					problem = new InfoflowLocalProblem(sources, sinks);
				} else{
					problem = new InfoflowProblem(sources, sinks);
				}
				 
				for (SootMethod ep : Scene.v().getEntryPoints()) {
					problem.initialSeeds.add(ep.getActiveBody().getUnits().getFirst());
				}

				//JimpleIFDSSolver<Abstraction> solver = new JimpleIFDSSolver<Abstraction>(problem);
				JimpleIFDSSolver<Abstraction,InterproceduralCFG<Unit, SootMethod>> solver =
						new JimpleIFDSSolver<Abstraction,InterproceduralCFG<Unit, SootMethod>>(problem);

				
				solver.solve(0);
				solver.dumpResults(); // only for debugging

				for (SootMethod ep : Scene.v().getEntryPoints()) {

					Unit ret = ep.getActiveBody().getUnits().getLast();
					System.err.println(ep.getActiveBody());

					System.err.println("----------------------------------------------");
					System.err.println("At end of: " + ep.getSignature());
					System.err.println(solver.ifdsResultsAt(ret).size() + " Variables:");
					System.err.println("----------------------------------------------");

					for (Abstraction l : solver.ifdsResultsAt(ret)) {
						System.err.println(l.getCorrespondingMethod() +": "+ l.getAccessPath() + " contains value from " + l.getSource());
					}
					System.err.println("---");
				}
				
				results = problem.results;
				for(Entry<String, List<String>> entry : results.entrySet()){
					System.out.println("The sink " + entry.getKey() + " was called with values from the following sources:");
					for(String str : entry.getValue()){
						System.out.println("- " + str);
					}	
				}
			}
		});

		PackManager.v().getPack("wjtp").add(transform);
	}
	
	@Override
	public HashMap<String, List<String>> getResults(){
		return results;
	}
	
	@Override
	public void setLocalInfoflow(boolean local){
		this.local = local;
	}
	
	@Override
	public boolean isResultAvailable(){
		if(results == null){
			return false;
		}
		return true;
	}
}