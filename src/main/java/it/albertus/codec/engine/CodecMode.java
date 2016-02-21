package it.albertus.codec.engine;

public enum CodecMode {
	ENCODE(0, "Encode"),
	DECODE(1, "Decode");

	private final int index;
	private final String name;

	private CodecMode(int index, String name) {
		this.index = index;
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
