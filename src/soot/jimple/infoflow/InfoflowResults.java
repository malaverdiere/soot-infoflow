package soot.jimple.infoflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	}

	private final Map<String, List<SourceInfo>> results = new HashMap<String, List<SourceInfo>>();
	
	public InfoflowResults() {
		
	}
	
	public int size() {
		return this.results.size();
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
		List<SourceInfo> sourceInfo = this.results.get(sink);
		if (sourceInfo == null) {
			sourceInfo = new ArrayList<SourceInfo>();
			this.results.put(sink, sourceInfo);
		}
		sourceInfo.add(source);
	}

	public Map<String, List<SourceInfo>> getResults() {
		return this.results;
	}
	
	public boolean isPathBetween(String sink, String source) {
		List<SourceInfo> sources = this.results.get(sink);
		if (sources == null)
			return false;
		for (SourceInfo src : sources)
			if (src.source.equals(source))
				return true;
		return false;
	}

}
