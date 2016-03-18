package it.albertus.codec.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum CodecAlgorithm {
	BASE16("Base16"),
	BASE32("Base32"),
	BASE64("Base64"),
	MD2("MD2", CodecMode.ENCODE),
	MD4("MD4", CodecMode.ENCODE),
	MD5("MD5", CodecMode.ENCODE),
	SHA1("SHA-1", CodecMode.ENCODE),
	SHA256("SHA-256", CodecMode.ENCODE),
	SHA384("SHA-384", CodecMode.ENCODE),
	SHA512("SHA-512", CodecMode.ENCODE);

	private final String name;
	private final Set<CodecMode> modes;

	private CodecAlgorithm(final String name, final CodecMode... modes) {
		this.name = name;
		this.modes = new HashSet<CodecMode>(Arrays.asList(modes.length == 0 ? CodecMode.values() : modes));
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public Set<CodecMode> getModes() {
		return modes;
	}

	public static String[] getNames() {
		final String[] names = new String[CodecAlgorithm.values().length];
		for (int i = 0; i < CodecAlgorithm.values().length; i++) {
			names[i] = CodecAlgorithm.values()[i].name;
		}
		return names;
	}

}
