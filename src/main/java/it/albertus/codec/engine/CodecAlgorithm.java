package it.albertus.codec.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum CodecAlgorithm {
	BASE32(0, "Base32"),
	BASE64(1, "Base64"),
	MD2(2, "MD2", CodecMode.ENCODE),
	MD5(3, "MD5", CodecMode.ENCODE),
	SHA1(4, "SHA-1", CodecMode.ENCODE),
	SHA256(5, "SHA-256", CodecMode.ENCODE),
	SHA384(6, "SHA-384", CodecMode.ENCODE),
	SHA512(7, "SHA-512", CodecMode.ENCODE);

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
