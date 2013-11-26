/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow;

import heros.solver.CountingThreadPoolExecutor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowResults.SinkInfo;
import soot.jimple.infoflow.InfoflowResults.SourceInfo;
import soot.jimple.infoflow.aliasing.FlowSensitiveAliasStrategy;
import soot.jimple.infoflow.aliasing.IAliasingStrategy;
import soot.jimple.infoflow.aliasing.PtsBasedAliasStrategy;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator;
import soot.jimple.infoflow.entryPointCreators.IEntryPointCreator;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.handlers.TaintPropagationHandler;
import soot.jimple.infoflow.heros.InfoflowCFG;
import soot.jimple.infoflow.heros.InfoflowSolver;
import soot.jimple.infoflow.problems.BackwardsInfoflowProblem;
import soot.jimple.infoflow.problems.InfoflowProblem;
import soot.jimple.infoflow.source.DefaultSourceSinkManager;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.options.Options;
/**
 * main infoflow class which triggers the analysis and offers method to customize it.
 *
 */
public class Infoflow implements IInfoflow {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());

	private static boolean debug = false;
	private static int accessPathLength = 5;
	
	private InfoflowResults results;

	private final String androidPath;
	private final boolean forceAndroidJar;
	private ITaintPropagationWrapper taintWrapper;
	private IInfoflowConfig sootConfig;
	
	private boolean stopAfterFirstFlow = false;
	private boolean enableImplicitFlows = false;
	private boolean enableStaticFields = true;
	private boolean enableExceptions = true;
	private boolean computeResultPaths = true;
	private boolean flowSensitiveAliasing = true;
	
	private boolean inspectSources = false;
	private boolean inspectSinks = false;
	
	private int maxThreadNum = -1;
	
	private CallgraphAlgorithm callgraphAlgorithm = CallgraphAlgorithm.AutomaticSelection;
	private AliasingAlgorithm aliasingAlgorithm = AliasingAlgorithm.FlowSensitive;

    private BiDirICFGFactory icfgFactory = new DefaultBiDiICFGFactory();
    private List<Transform> preProcessors = Collections.emptyList();
    private InfoflowCFG iCfg;
    
    private Set<ResultsAvailableHandler> onResultsAvailable = new HashSet<ResultsAvailableHandler>();
    private Set<TaintPropagationHandler> taintPropagationHandlers = new HashSet<TaintPropagationHandler>();

	/**
	 * Creates a new instance of the InfoFlow class for analyzing plain Java code without any references to APKs or the Android SDK.
	 */
	public Infoflow() {
		this.androidPath = "";
		this.forceAndroidJar = false;
	}

	/**
	 * Creates a new instance of the InfoFlow class for analyzing Android APK files. This constructor sets the right options for analyzing APK files.
	 * 
	 * @param androidPath
	 *            If forceAndroidJar is false, this is the base directory of the platform files in the Android SDK. If forceAndroidJar is true, this is the full path of a single android.jar file.
	 * @param forceAndroidJar
	 */
	public Infoflow(String androidPath, boolean forceAndroidJar) {
		this.androidPath = androidPath;
		this.forceAndroidJar = forceAndroidJar;
	}

	public void setTaintWrapper(ITaintPropagationWrapper wrapper) {
		taintWrapper = wrapper;
	}

	public static void setDebug(boolean debugflag) {
		debug = debugflag;
	}

	@Override
	public void setInspectSources(boolean inspect){
		inspectSources = inspect;
	}

	@Override
	public void setInspectSinks(boolean inspect){
		inspectSinks = inspect;
	}
	
	@Override
	public void setEnableImplicitFlows(boolean enableImplicitFlows) {
		this.enableImplicitFlows = enableImplicitFlows;
	}

	@Override
	public void setEnableStaticFieldTracking(boolean enableStaticFields) {
		this.enableStaticFields = enableStaticFields;
	}

	@Override
	public void setEnableExceptionTracking(boolean enableExceptions) {
		this.enableExceptions = enableExceptions;
	}

	@Override
	public void setComputeResultPaths(boolean computeResultPaths) {
		this.computeResultPaths = computeResultPaths;
	}

	@Override
	public void setFlowSensitiveAliasing(boolean flowSensitiveAliasing) {
		this.flowSensitiveAliasing = flowSensitiveAliasing;
	}

	public void setSootConfig(IInfoflowConfig config){
		sootConfig = config;
	}
	
	@Override
	public void setStopAfterFirstFlow(boolean stopAfterFirstFlow) {
		this.stopAfterFirstFlow = stopAfterFirstFlow;
	}

    @Override
    public void setIcfgFactory(BiDirICFGFactory factory){
        this.icfgFactory = factory;
    }

    @Override
    public void setPreProcessors(List<Transform> preprocessors){
        this.preProcessors = preprocessors;
    }
    
    @Override
    public void setCallgraphAlgorithm(CallgraphAlgorithm algorithm) {
    	this.callgraphAlgorithm = algorithm;
    }
	
    @Override
    public void setAliasingAlgorithm(AliasingAlgorithm algorithm) {
    	this.aliasingAlgorithm = algorithm;
    }

    @Override
	public void computeInfoflow(String path, IEntryPointCreator entryPointCreator,
			List<String> entryPoints, List<String> sources, List<String> sinks) {
		this.computeInfoflow(path, entryPointCreator, entryPoints,
				new DefaultSourceSinkManager(sources, sinks));
	}
	
	@Override
	public void computeInfoflow(String path, List<String> entryPoints, List<String> sources, List<String> sinks) {
		this.computeInfoflow(path, new DefaultEntryPointCreator(), entryPoints, new DefaultSourceSinkManager(sources, sinks));
	}

	@Override
	public void computeInfoflow(String path, String entryPoint, List<String> sources, List<String> sinks) {
		this.computeInfoflow(path, entryPoint, new DefaultSourceSinkManager(sources, sinks));
	}
	
	/**
	 * Initializes Soot.
	 * @param path The Soot classpath
	 * @param classes The set of classes that shall be checked for data flow
	 * analysis seeds. All sources in these classes are used as seeds.
	 * @param sourcesSinks The manager object for identifying sources and sinks
	 */
	private void initializeSoot(String path, Set<String> classes, ISourceSinkManager sourcesSinks) {
		initializeSoot(path, classes, sourcesSinks, "");
	}
	
	/**
	 * Initializes Soot.
	 * @param path The Soot classpath
	 * @param classes The set of classes that shall be checked for data flow
	 * analysis seeds. All sources in these classes are used as seeds. If a
	 * non-empty extra seed is given, this one is used too.
	 * @param sourcesSinks The manager object for identifying sources and sinks
	 * @param extraSeed An optional extra seed, can be empty.
	 */
	private void initializeSoot(String path, Set<String> classes, ISourceSinkManager sourcesSinks, String extraSeed) {
		// reset Soot:
		logger.info("Resetting Soot...");
		soot.G.reset();
		
		// add SceneTransformer which calculates and prints infoflow
		Set<String> seeds = Collections.emptySet();
		if (extraSeed != null && !extraSeed.isEmpty())
			seeds = Collections.singleton(extraSeed);
		addSceneTransformer(sourcesSinks, seeds);

		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		if (debug)
			Options.v().set_output_format(Options.output_format_jimple);
		else
			Options.v().set_output_format(Options.output_format_none);
		Options.v().set_whole_program(true);
		Options.v().set_soot_classpath(path);
		Options.v().set_process_dir(new ArrayList<String>(classes));

		// Configure the callgraph algorithm
		switch (callgraphAlgorithm) {
			case AutomaticSelection:
				if (extraSeed == null || extraSeed.isEmpty())
					Options.v().setPhaseOption("cg.spark", "on");
				else
					Options.v().setPhaseOption("cg.spark", "vta:true");
				break;
			case RTA:
				Options.v().setPhaseOption("cg.spark", "on");
				Options.v().setPhaseOption("cg.spark", "rta:true");
				break;
			case VTA:
				Options.v().setPhaseOption("cg.spark", "on");
				Options.v().setPhaseOption("cg.spark", "vta:true");
				break;
			default:
				throw new RuntimeException("Invalid callgraph algorithm");
		}
		// do not merge variables (causes problems with PointsToSets)
		Options.v().setPhaseOption("jb.ulp", "off");
		
		Options.v().setPhaseOption("cg", "trim-clinit:false");
		
		if (!this.androidPath.isEmpty()) {
			Options.v().set_src_prec(Options.src_prec_apk);
			if (this.forceAndroidJar)
				soot.options.Options.v().set_force_android_jar(this.androidPath);
			else
				soot.options.Options.v().set_android_jars(this.androidPath);
		} else
			Options.v().set_src_prec(Options.src_prec_java);
		
		//at the end of setting: load user settings:
		if (sootConfig != null)
			sootConfig.setSootOptions(Options.v());
		
		// load all entryPoint classes with their bodies
		Scene.v().loadNecessaryClasses();
		boolean hasClasses = false;
		for (String className : classes) {
			SootClass c = Scene.v().forceResolve(className, SootClass.BODIES);
			if (c != null){
				c.setApplicationClass();
				if(!c.isPhantomClass() && !c.isPhantom())
					hasClasses = true;
			}
		}
		if (!hasClasses) {
			logger.error("Only phantom classes loaded, skipping analysis...");
			return;
		}

	}

	@Override
	public void computeInfoflow(String path, IEntryPointCreator entryPointCreator,
			List<String> entryPoints, ISourceSinkManager sourcesSinks) {
		results = null;
		if (sourcesSinks == null) {
			logger.error("Sources are empty!");
			return;
		}
	
		initializeSoot(path,
				SootMethodRepresentationParser.v().parseClassNames(entryPoints, false).keySet(),
				sourcesSinks);

		// entryPoints are the entryPoints required by Soot to calculate Graph - if there is no main method,
		// we have to create a new main method and use it as entryPoint and store our real entryPoints
		Scene.v().setEntryPoints(Collections.singletonList(entryPointCreator.createDummyMain(entryPoints)));
		PackManager.v().runPacks();
		if (debug)
			PackManager.v().writeOutput();
	}


	@Override
	public void computeInfoflow(String path, String entryPoint, ISourceSinkManager sourcesSinks) {
		results = null;
		if (sourcesSinks == null) {
			logger.error("Sources are empty!");
			return;
		}

		// parse classNames as String and methodNames as string in soot representation
		HashMap<String, List<String>> classes = SootMethodRepresentationParser.v().parseClassNames
						(Collections.singletonList(entryPoint), false);

		initializeSoot(path, classes.keySet(), sourcesSinks, entryPoint);
		
		if (debug) {
			for (List<String> methodList : classes.values()) {
				for (String methodSignature : methodList) {
					if (Scene.v().containsMethod(methodSignature)) {
						SootMethod method = Scene.v().getMethod(methodSignature);
						logger.debug(method.retrieveActiveBody().toString());
					}
				}
			}
		}

		if (!Scene.v().containsMethod(entryPoint)){
			logger.error("Entry point not found: " + entryPoint);
			return;
		}
		SootMethod ep = Scene.v().getMethod(entryPoint);
		if (ep.isConcrete())
			ep.retrieveActiveBody();
		else {
			logger.debug("Skipping non-concrete method " + ep);
			return;
		}
		Scene.v().setEntryPoints(Collections.singletonList(ep));
		Options.v().set_main_class(ep.getDeclaringClass().getName());
		PackManager.v().runPacks();
		if (debug)
			PackManager.v().writeOutput();
	}

	private void addSceneTransformer(final ISourceSinkManager sourcesSinks, final Set<String> additionalSeeds) {
		Transform transform = new Transform("wjtp.ifds", new SceneTransformer() {
			protected void internalTransform(String phaseName, @SuppressWarnings("rawtypes") Map options) {
                logger.info("Callgraph has {} edges", Scene.v().getCallGraph().size());
                iCfg = icfgFactory.buildBiDirICFG();
                
                int numThreads = Runtime.getRuntime().availableProcessors();

				CountingThreadPoolExecutor executor = new CountingThreadPoolExecutor
						(maxThreadNum == -1 ? numThreads : Math.min(maxThreadNum, numThreads),
						Integer.MAX_VALUE, 30, TimeUnit.SECONDS,
						new LinkedBlockingQueue<Runnable>());

				BackwardsInfoflowProblem backProblem;
				InfoflowSolver backSolver;
				final IAliasingStrategy aliasingStrategy;
				switch (aliasingAlgorithm) {
					case FlowSensitive:
						backProblem = new BackwardsInfoflowProblem();
						backSolver = new InfoflowSolver(backProblem, executor);
						aliasingStrategy = new FlowSensitiveAliasStrategy(iCfg, backSolver);
						break;
					case PtsBased:
						backProblem = null;
						backSolver = null;
						aliasingStrategy = new PtsBasedAliasStrategy(iCfg);
						break;
					default:
						throw new RuntimeException("Unsupported aliasing algorithm");
				}

				InfoflowProblem forwardProblem  = new InfoflowProblem(iCfg, sourcesSinks, aliasingStrategy);
				
				// We have to look through the complete program to find sources
				// which are then taken as seeds.
				int sinkCount = 0;
                logger.info("Looking for sources and sinks...");

				List<MethodOrMethodContext> eps = new ArrayList<MethodOrMethodContext>(Scene.v().getEntryPoints());
				ReachableMethods reachableMethods = new ReachableMethods(Scene.v().getCallGraph(), eps.iterator(), null);
				reachableMethods.update();
				Map<String, String> classes = new HashMap<String, String>(10000);
				for(Iterator<MethodOrMethodContext> iter = reachableMethods.listener(); iter.hasNext(); ) {
					SootMethod m = iter.next().method();
					if (m.hasActiveBody()) {
						// In Debug mode, we collect the Jimple bodies for
						// writing them to disk later
						if (debug)
							if (classes.containsKey(m.getDeclaringClass().getName()))
								classes.put(m.getDeclaringClass().getName(), classes.get(m.getDeclaringClass().getName())
										+ m.getActiveBody().toString());
							else
								classes.put(m.getDeclaringClass().getName(), m.getActiveBody().toString());
						
						// Look for a source in the method. Also look for sinks. If we
						// have no sink in the program, we don't need to perform any
						// analysis
						PatchingChain<Unit> units = m.getActiveBody().getUnits();
						for (Unit u : units) {
							Stmt s = (Stmt) u;
							if (sourcesSinks.isSource(s, iCfg)) {
								forwardProblem.addInitialSeeds(u, Collections.singleton(forwardProblem.zeroValue()));
								logger.debug("Source found: {}", u);
							}
							if (sourcesSinks.isSink(s, iCfg)) {
                                logger.debug("Sink found: {}", u);
								sinkCount++;
							}
						}
						
					}
				}
				
				// We optionally also allow additional seeds to be specified
				if (additionalSeeds != null)
					for (String meth : additionalSeeds) {
						SootMethod m = Scene.v().getMethod(meth);
						if (!m.hasActiveBody()) {
							logger.warn("Seed method {} has no active body", m);
							continue;
						}
						forwardProblem.addInitialSeeds(m.getActiveBody().getUnits().getFirst(),
								Collections.singleton(forwardProblem.zeroValue()));
					}

				// In Debug mode, we write the Jimple files to disk
				if (debug){
					File dir = new File("JimpleFiles");
					if(!dir.exists()){
						dir.mkdir();
					}
					for (Entry<String, String> entry : classes.entrySet()) {
						try {
							stringToTextFile(new File(".").getAbsolutePath() + System.getProperty("file.separator") +"JimpleFiles"+ System.getProperty("file.separator") + entry.getKey() + ".jimple", entry.getValue());
						} catch (IOException e) {
							logger.error("Could not write jimple file: {}", entry.getKey() + ".jimple", e);
						}
					}
				}

				if (!forwardProblem.hasInitialSeeds() || sinkCount == 0){
					logger.error("No sources or sinks found, aborting analysis");
					return;
				}

				logger.info("Source lookup done, found {} sources and {} sinks.", forwardProblem.getInitialSeeds().size(),
						sinkCount);
				
				InfoflowSolver forwardSolver = new InfoflowSolver(forwardProblem, executor);
				aliasingStrategy.setForwardSolver(forwardSolver);
				
				forwardProblem.setInspectSources(inspectSources);
				forwardProblem.setInspectSinks(inspectSinks);
				forwardProblem.setEnableImplicitFlows(enableImplicitFlows);
				forwardProblem.setEnableStaticFieldTracking(enableStaticFields);
				forwardProblem.setEnableExceptionTracking(enableExceptions);
				for (TaintPropagationHandler tp : taintPropagationHandlers)
					forwardProblem.addTaintPropagationHandler(tp);
				forwardProblem.setFlowSensitiveAliasing(flowSensitiveAliasing);
				forwardProblem.setTaintWrapper(taintWrapper);
				forwardProblem.setStopAfterFirstFlow(stopAfterFirstFlow);
				
				if (backProblem != null) {
					backProblem.setForwardSolver((InfoflowSolver) forwardSolver);
					backProblem.setTaintWrapper(taintWrapper);
					backProblem.setZeroValue(forwardProblem.createZeroValue());
					backProblem.setEnableStaticFieldTracking(enableStaticFields);
					backProblem.setEnableExceptionTracking(enableExceptions);
					for (TaintPropagationHandler tp : taintPropagationHandlers)
						backProblem.addTaintPropagationHandler(tp);
					backProblem.setFlowSensitiveAliasing(flowSensitiveAliasing);
					backProblem.setTaintWrapper(taintWrapper);
				}
				
				if (!enableStaticFields)
					logger.warn("Static field tracking is disabled, results may be incomplete");
				if (!flowSensitiveAliasing)
					logger.warn("Using flow-insensitive alias tracking, results may be imprecise");

				forwardSolver.solve();
				
				// Not really nice, but sometimes Heros returns before all
				// executor tasks are actually done. This way, we give it a
				// chance to terminate gracefully before moving on.
				int terminateTries = 0;
				while (terminateTries < 10) {
					if (executor.getActiveCount() != 0 || !executor.isTerminated()) {
						terminateTries++;
						try {
							Thread.sleep(500);
						}
						catch (InterruptedException e) {
							logger.error("Could not wait for executor termination", e);
						}
					}
					else
						break;
				}
				if (executor.getActiveCount() != 0 || !executor.isTerminated())
					logger.error("Executor did not terminate gracefully");

				// Print taint wrapper statistics
				if (taintWrapper != null) {
					logger.info("Taint wrapper hits: " + taintWrapper.getWrapperHits());
					logger.info("Taint wrapper misses: " + taintWrapper.getWrapperMisses());
				}
				
				logger.info("IFDS problem solved, processing results...");
				
				// Force a cleanup. Everything we need is reachable through the
				// results set, the other abstractions can be killed now.
				forwardSolver.cleanup();
				if (backSolver != null) {
					backSolver.cleanup();
					backSolver = null;
				}
				forwardSolver = null;
				Runtime.getRuntime().gc();
				
				results = forwardProblem.getResults(computeResultPaths);
				
				if (results.getResults().isEmpty())
					logger.warn("No results found.");
				else for (Entry<SinkInfo, Set<SourceInfo>> entry : results.getResults().entrySet()) {
					logger.info("The sink {} in method {} was called with values from the following sources:",
                            entry.getKey(), iCfg.getMethodOf(entry.getKey().getContext()).getSignature() );
					for (SourceInfo source : entry.getValue()) {
						logger.info("- {} in method {}",source, iCfg.getMethodOf(source.getContext()).getSignature());
						if (source.getPath() != null && !source.getPath().isEmpty()) {
							logger.info("\ton Path: ");
							for (Unit p : source.getPath()) {
								logger.info("\t\t -> " + p);
							}
						}
					}
				}
				
				for (ResultsAvailableHandler handler : onResultsAvailable)
					handler.onResultsAvailable(iCfg, results);
			}

			
		});

        for (Transform tr : preProcessors){
            PackManager.v().getPack("wjtp").add(tr);
        }
		PackManager.v().getPack("wjtp").add(transform);
	}

		private void stringToTextFile(String fileName, String contents) throws IOException {
			BufferedWriter wr = null;
			try {
				wr = new BufferedWriter(new FileWriter(fileName));
				wr.write(contents);
				wr.flush();
			}
			finally {
				if (wr != null)
					wr.close();
			}
		}

	@Override
	public InfoflowResults getResults() {
		return results;
	}

	@Override
	public boolean isResultAvailable() {
		if (results == null) {
			return false;
		}
		return true;
	}

	
	public static int getAccessPathLength() {
		return accessPathLength;
	}
	
	@Override
	public void setAccessPathLength(int accessPathLength) {
		Infoflow.accessPathLength = accessPathLength;
	}
	
	/**
	 * Adds a handler that is called when information flow results are available
	 * @param handler The handler to add
	 */
	public void addResultsAvailableHandler(ResultsAvailableHandler handler) {
		this.onResultsAvailable.add(handler);
	}
	
	/**
	 * Adds a handler which is invoked whenever a taint is propagated
	 * @param handler The handler to be invoked when propagating taints
	 */
	public void addTaintPropagationHandler(TaintPropagationHandler handler) {
		this.taintPropagationHandlers.add(handler);
	}
	
	/**
	 * Removes a handler that is called when information flow results are available
	 * @param handler The handler to remove
	 */
	public void removeResultsAvailableHandler(ResultsAvailableHandler handler) {
		onResultsAvailable.remove(handler);
	}
	
	@Override
	public void setMaxThreadNum(int threadNum) {
		this.maxThreadNum = threadNum;
	}

}
