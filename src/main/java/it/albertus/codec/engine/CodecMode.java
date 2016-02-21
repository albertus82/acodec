package it.albertus.codec.engine;

import it.albertus.codec.resources.Resources;

public enum CodecMode {
	ENCODE(0, "lbl.mode.encode", 'E'),
	DECODE(1, "lbl.mode.decode", 'D');

	private final int index;
	private final String label;
	private final char abbreviation;

	private CodecMode(final int index, final String key, final char abbreviation) {
		this.index = index;
		this.label = Resources.get(key);
		this.abbreviation = abbreviation;
	}

	public int getIndex() {
		return index;
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
