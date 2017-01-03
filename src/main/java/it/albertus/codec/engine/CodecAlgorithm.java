package it.albertus.codec.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum CodecAlgorithm {
	BASE16("Base16"),
	BASE32("Base32"),
	BASE64("Base64"),
	ASCII85("Ascii85"),
	BASE91("basE91"),
	MD2("MD2", true),
	MD4("MD4", true),
	MD5("MD5", true),
	SHA1("SHA-1", true),
	SHA256("SHA-256", true),
	SHA384("SHA-384", true),
	SHA512("SHA-512", true);

	private final String name;
	private final boolean digest;
	private final Set<CodecMode> modes;

	private CodecAlgorithm(final String name, final boolean digest, final CodecMode... modes) {
		this.name = name;
		this.digest = digest;
		this.modes = new HashSet<CodecMode>(Arrays.asList(modes));
	}

	private CodecAlgorithm(final String name, final boolean digest) {
		this(name, digest, digest ? new CodecMode[] { CodecMode.ENCODE } : CodecMode.values());
	}

	private CodecAlgorithm(final String name) {
		this(name, false, CodecMode.values());
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

	public final boolean isDigest() {
		return digest;
	}

	public static String[] getNames() {
		final String[] names = new String[CodecAlgorithm.values().length];
		for (int i = 0; i < CodecAlgorithm.values().length; i++) {
			names[i] = CodecAlgorithm.values()[i].name;
		}
		return names;
	}

}
