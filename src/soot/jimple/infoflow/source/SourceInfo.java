package soot.jimple.infoflow.source;

/**
 * Class containing additional information about a source. Users of FlowDroid
 * can derive from this class when implementing their own SourceSinkManager
 * to associate additional information with a source.
 * 
 * @author Steven Arzt
 */
public class SourceInfo {
	
	private boolean taintSubFields = true;
	
	/**
	 * Creates a new instance of the {@link SourceInfo} class
	 * @param taintSubFields True if all fields reachable through the source
	 * shall also be considered as tainted, false if only the source as such
	 * shall be tainted.
	 */
	public SourceInfo(boolean taintSubFields) {
		this.taintSubFields = taintSubFields;
	}
	
	@Override
	public int hashCode() {
		return 31 * (taintSubFields ? 1 : 0);
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof SourceInfo))
			return false;
		SourceInfo otherInfo = (SourceInfo) other;
		return taintSubFields == otherInfo.taintSubFields;
	}

	/**
	 * Gets whether all fields reachable through the source shall also be
	 * considered as tainted.
	 * @return True if all fields reachable through the source shall also
	 * be considered as tainted, false if only the source as such shall be
	 * tainted.
	 */
	public boolean getTaintSubFields() {
		return taintSubFields;
	}

}
