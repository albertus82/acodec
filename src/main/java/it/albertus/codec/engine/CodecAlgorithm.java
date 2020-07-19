package it.albertus.codec.engine;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;

public enum CodecAlgorithm {
	BASE16("Base16"),
	BASE32("Base32"),
	BASE64("Base64"),
	ASCII85("Ascii85"),
	BASE91("basE91"),
	CRC16("CRC-16", true),
	CRC32("CRC-32", true),
	MD2(MessageDigestAlgorithms.MD2, true),
	MD4("MD4", true),
	MD5(MessageDigestAlgorithms.MD5, true),
	SHA1(MessageDigestAlgorithms.SHA_1, true),
	SHA224(MessageDigestAlgorithms.SHA_224, true),
	SHA256(MessageDigestAlgorithms.SHA_256, true),
	SHA384(MessageDigestAlgorithms.SHA_384, true),
	SHA512(MessageDigestAlgorithms.SHA_512, true),
	SHA512_224(MessageDigestAlgorithms.SHA_512_224, true),
	SHA512_256(MessageDigestAlgorithms.SHA_512_256, true),
	SHA3_224(MessageDigestAlgorithms.SHA3_224, true),
	SHA3_256(MessageDigestAlgorithms.SHA3_256, true),
	SHA3_384(MessageDigestAlgorithms.SHA3_384, true),
	SHA3_512(MessageDigestAlgorithms.SHA3_512, true);

	private final String name;
	private final boolean digest;
	private final Set<CodecMode> modes;

	private CodecAlgorithm(final String name, final boolean digest, final CodecMode... modes) {
		this.name = name;
		this.digest = digest;
		this.modes = EnumSet.copyOf(Arrays.asList(modes));
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
