package soot.jimple.infoflow;

import java.util.Arrays;
import java.util.List;

import soot.jimple.infoflow.util.ArgParser;
/**
 * cmdInfoflow offers a command-line interface to start the analysis.
 * However, it is not efficiently usable for large amounts of entry points, sources and sinks.
 * Therefore, most use-cases access the java classes directly.
 *
 */
public class cmdInfoflow {

	public static void main(String[] args) {
		ArgParser parser = new ArgParser();
		if (args.length > 0) {
			if (Arrays.asList(args).contains(ArgParser.METHODKEYWORD)) {
				List<List<String>> inputArgs = parser.parseClassArguments(args);

				if (inputArgs.get(0) == null
						|| inputArgs.size() < 3) {
					System.err.println("Arguments could not be parsed!");
					return;
				}
				IInfoflow infoflow = new Infoflow();
				infoflow.computeInfoflow(inputArgs.get(3).get(0), inputArgs.get(0), inputArgs.get(1), inputArgs.get(2));
			}
		}
	}

}
