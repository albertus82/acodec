package it.albertus.codec.engine;

public enum CodecCharset {
	ISO_8859_1("ISO-8859-1"),
	US_ASCII("US-ASCII"),
	UTF_16("UTF-16"),
	UTF_16BE("UTF-16BE"),
	UTF_16LE("UTF-16LE"),
	UTF_8("UTF-8");

	private final String name;

	private CodecCharset(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static String[] getNames() {
		final String[] names = new String[CodecCharset.values().length];
		for (int i = 0; i < CodecCharset.values().length; i++) {
			names[i] = CodecCharset.values()[i].name;
		}
		return names;
	}

}
