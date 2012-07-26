package soot.jimple.infoflow.util;

public class ArgBuilder {
	/**
	 * build the arguments
	 * at the moment this is build: -w -p cg.spark on -cp . -pp [className]
	 * @param input
	 * @return
	 */
	public String[] buildArgs(String className){
		String[] result = new String[8];
		result[0] = "-w";
		result[1] = "-p";
		result[2] = "cg.spark";
		result[3] = "on";
		result[4] = "-cp";
		result[5] = ".";
		result[6] = "-pp";
		result[7] = className;
		
		return result;
	}
	
	/**TODO
	 * -android-jars F:\master\android-platforms\platforms
		-src-prec apk
		-cp F:\master\QueryContacts.apk -pp
		com.appsolut.example.queryContacts.MainActivity
	 * @param apkPath
	 * @return
	 */
	public String[] buildArgsForAndroid(String apkPath){
		String[] result = new String[0];
		
		
		return result;
	}

}
