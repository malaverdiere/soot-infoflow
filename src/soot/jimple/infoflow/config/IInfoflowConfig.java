package soot.jimple.infoflow.config;
/**
 * Interface to configure Soot options like the output format or a list of packages that should be included or excluded for analysis
 * @author Christian
 *
 */
public interface IInfoflowConfig {

	/**
	 * Configure Soot options (Be careful, wrong options can corrupt the analysis results!)
	 * @param options the singleton to configure soot options
	 * 
	 */
	public void setSootOptions(soot.options.Options options);
}
