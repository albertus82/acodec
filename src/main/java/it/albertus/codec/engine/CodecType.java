package it.albertus.codec.engine;

public enum CodecType {
	BASE64(0, "Base64");

	private final int index;
	private final String name;

	private CodecType(int index, String name) {
		this.index = index;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public static String[] getNames() {
		String[] names = new String[CodecType.values().length];
		for (int i = 0; i < CodecType.values().length; i++) {
			names[i] = CodecType.values()[i].name;
		}
		return names;
	}

}