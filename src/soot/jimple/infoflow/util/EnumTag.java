package soot.jimple.infoflow.util;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

/**
 * Class for storing enumeration values in Soot's tag collection
 * 
 * @author Steven Arzt
 */
public class EnumTag<E extends Enum<?>> implements Tag {
	
	private final String tagName;
	private final E value;
	
	public EnumTag(String tagName, E value) {
		this.tagName = tagName;
		this.value = value;
	}
	
	@Override
	public String getName() {
		return tagName;
	}

	@Override
	public byte[] getValue() throws AttributeValueException {
		return new byte[] { (byte) this.value.ordinal() };
	}
	
}