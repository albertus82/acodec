package it.albertus.codec.engine;

public enum CodecMode {
	ENCODE(0, "Encode", 'E'),
	DECODE(1, "Decode", 'D');

	private final int index;
	private final String name;
	private final char abbreviation;

	private CodecMode(final int index, final String name, final char abbreviation) {
		this.index = index;
		this.name = name;
		this.abbreviation = abbreviation;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public char getAbbreviation() {
		return abbreviation;
	}

	@Override
	public String toString() {
		return name;
	}

}
