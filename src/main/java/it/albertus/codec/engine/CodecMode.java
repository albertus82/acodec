package it.albertus.codec.engine;

import it.albertus.codec.resources.Messages;

public enum CodecMode {
	ENCODE("lbl.mode.encode", 'e'),
	DECODE("lbl.mode.decode", 'd');

	private final String label;
	private final char abbreviation;

	private CodecMode(final String key, final char abbreviation) {
		this.label = Messages.get(key);
		this.abbreviation = abbreviation;
	}

	public String getName() {
		return label;
	}

	public char getAbbreviation() {
		return abbreviation;
	}

	@Override
	public String toString() {
		return label;
	}

}
