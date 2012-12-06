package soot.jimple.infoflow.util;

public class ArgBuilder {
	/**
	 * build the arguments
	 * @param input
	 * @return
	 */
	public String[] buildArgs(String path, String className){
		String[] result = {
			"-w",
			"-no-bodies-for-excluded",
			"-p",
			"cg.spark",
			"on",
			"-cp",
			path,
			"-pp",
			className,
			"-p",
			"jb",
			"use-original-names:true",
			"-f",
			"n",
			//do not merge variables (causes problems with PointsToSets)
			"-p",
			"jb.ulp",
			"off"
		};
		
		return result;
	}

}
