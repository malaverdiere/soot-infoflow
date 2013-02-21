package soot.jimple.infoflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for collecting information flow results
 * @author sarzt
 *
 */
public class InfoflowResults {
	
	/**
	 * Class for modeling information flowing out of a specific source
	 * @author sarzt
	 *
	 */
	public class SourceInfo {
		private final String source;
		private final List<String> path;
		
		public SourceInfo(String source) {
			this.source = source;
			this.path = null;
		}
		
		public SourceInfo(String source, List<String> path) {
			this.source = source;
			this.path = path;
		}

		public String getSource() {
			return this.source;
		}
		
		public List<String> getPath() {
			return this.path;
		}
		
		@Override
		public int hashCode() {
			return 31 * this.source.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (super.equals(o))
				return true;
			if (o == null || !(o instanceof SourceInfo))
				return false;
			SourceInfo si = (SourceInfo) o;
			return this.source.equals(si.source);
		}
	}

	private final Map<String, Set<SourceInfo>> results = new HashMap<String, Set<SourceInfo>>();
	
	public InfoflowResults() {
		
	}
	
	public int size() {
		return this.results.size();
	}
	
	public boolean isEmpty() {
		return this.results.isEmpty();
	}
	
	public boolean containsSink(String sink) {
		return this.getResults().containsKey(sink);
	}
	
	public void addResult(String sink, String source) {
		this.addResult(sink, new SourceInfo(source));
	}
	
	public void addResult(String sink, String source, List<String> propagationPath) {
		this.addResult(sink, new SourceInfo(source, propagationPath));
	}

	public void addResult(String sink, String source, List<String> propagationPath, String stmt) {
		List<String> newPropPath = new ArrayList<String>(propagationPath);
		newPropPath.add(stmt);
		this.addResult(sink, new SourceInfo(source, newPropPath));
	}

	public void addResult(String sink, SourceInfo source) {
		Set<SourceInfo> sourceInfo = this.results.get(sink);
		if (sourceInfo == null) {
			sourceInfo = new HashSet<SourceInfo>();
			this.results.put(sink, sourceInfo);
		}
		sourceInfo.add(source);
	}

	public Map<String, Set<SourceInfo>> getResults() {
		return this.results;
	}
	
	/**
	 * Checks whether there is a path between the given source and sink.
	 * @param sink The sink to which there may be a path
	 * @param source The source from which there may be a path
	 * @return True if there is a path between the given source and sink, false
	 * otherwise
	 */
	public boolean isPathBetween(String sink, String source) {
		Set<SourceInfo> sources = this.results.get(sink);
		if (sources == null)
			return false;
		for (SourceInfo src : sources)
			if (src.source.equals(source))
				return true;
		return false;
	}
	
	/**
	 * in contrast to isPathBetween-Method, this method does not require a
	 * specific source-call but the soot-signature of the source method. 
	 * @param sink The sink to which there may be a path
	 * @param source The source from which there may be a path
	 * @return True if there is a path between the given source and sink, false
	 * otherwise
	 */
	public boolean isPathBetweenSourceMethod(String sink, String source) {
		Set<SourceInfo> sources = this.results.get(sink);
		if (sources == null)
			return false;
		for (SourceInfo src : sources)
			if (src.source.contains("."+source))
				return true;
		return false;
	}

	public void printResults() {
		for (String sink : this.results.keySet()) {
			System.out.println("Found a flow to sink" + sink + " from the following sources:");
			for (SourceInfo source : this.results.get(sink))
				System.out.println("\t- " + source.getSource());
		}
	}

}
