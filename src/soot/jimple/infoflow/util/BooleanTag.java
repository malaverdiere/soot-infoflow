package soot.jimple.infoflow.util;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * Class for storing boolean values in Soot's tag collection
 * 
 * @author Steven Arzt
 */
public class BooleanTag implements Tag {
	
	private final String tagName;
	private final boolean value;
	
	public BooleanTag(String tagName, boolean value) {
		this.tagName = tagName;
		this.value = value;
	}
	
	@Override
	public String getName() {
		return tagName;
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		return new byte[] { value ? (byte) 1 : (byte) 0 };
	}
	
}