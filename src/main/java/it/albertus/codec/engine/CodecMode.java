package it.albertus.codec.engine;

import it.albertus.codec.resources.Messages;

public enum CodecMode {
	ENCODE("lbl.mode.encode", 'e'),
	DECODE("lbl.mode.decode", 'd');

	private final String labelKey;
	private final char abbreviation;

	private CodecMode(final String labelKey, final char abbreviation) {
		this.labelKey = labelKey;
		this.abbreviation = abbreviation;
	}

	public String getName() {
		return Messages.get(labelKey);
	}

	public char getAbbreviation() {
		return abbreviation;
	}

	@Override
	public String toString() {
		return getName();
	}

}
