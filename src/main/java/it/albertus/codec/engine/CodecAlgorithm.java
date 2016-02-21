package it.albertus.codec.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum CodecAlgorithm {
	BASE64(0, "Base64"),
	MD2(1, "MD2", CodecMode.ENCODE),
	MD5(2, "MD5", CodecMode.ENCODE),
	SHA1(3, "SHA-1", CodecMode.ENCODE),
	SHA256(4, "SHA-256", CodecMode.ENCODE),
	SHA384(5, "SHA-384", CodecMode.ENCODE),
	SHA512(6, "SHA-512", CodecMode.ENCODE);

	private final int index;
	private final String name;
	private final Set<CodecMode> modes;

	private CodecAlgorithm(final int index, final String name, final CodecMode... modes) {
		this.index = index;
		this.name = name;
		this.modes = new HashSet<CodecMode>(Arrays.asList(modes.length == 0 ? CodecMode.values() : modes));
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

	public Set<CodecMode> getModes() {
		return modes;
	}

	public static String[] getNames() {
		String[] names = new String[CodecAlgorithm.values().length];
		for (int i = 0; i < CodecAlgorithm.values().length; i++) {
			names[i] = CodecAlgorithm.values()[i].name;
		}
		return names;
	}

}
