package it.albertus.acodec.common.engine;

import static it.albertus.acodec.common.engine.AlgorithmType.CHECKSUM;
import static it.albertus.acodec.common.engine.AlgorithmType.ENCODING;
import static it.albertus.acodec.common.engine.AlgorithmType.HASH;
import static it.albertus.acodec.common.engine.CodecMode.ENCODE;
import static java.lang.Thread.MIN_PRIORITY;

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import lombok.Getter;
import lombok.extern.java.Log;

@Log
@Getter
public enum CodecAlgorithm {

	BASE16("Base16", ENCODING),
	BASE32("Base32", ENCODING),
	BASE32HEX("base32hex", ENCODING),
	BASE64("Base64", "b64", ENCODING),
	BASE64URL("base64url", ENCODING),
	ASCII85("Ascii85", ENCODING, "BASE85"),
	BASE91("basE91", ENCODING),
	CRC16("CRC-16", CHECKSUM),
	CRC32("CRC-32", "sfv", CHECKSUM),
	CRC32C("CRC-32C", CHECKSUM),
	ADLER32("Adler-32", CHECKSUM),
	MD2(MessageDigestAlgorithms.MD2, HASH),
	MD4("MD4", HASH),
	MD5(MessageDigestAlgorithms.MD5, HASH),
	SHA_1(MessageDigestAlgorithms.SHA_1, HASH, "SHA1"),
	SHA_224(MessageDigestAlgorithms.SHA_224, HASH, "SHA224"),
	SHA_256(MessageDigestAlgorithms.SHA_256, HASH, "SHA256"),
	SHA_384(MessageDigestAlgorithms.SHA_384, HASH, "SHA384"),
	SHA_512(MessageDigestAlgorithms.SHA_512, HASH, "SHA512"),
	SHA_512_224(MessageDigestAlgorithms.SHA_512_224, "sha512-224", HASH, "SHA512-224"),
	SHA_512_256(MessageDigestAlgorithms.SHA_512_256, "sha512-256", HASH, "SHA512-256"),
	SHA3_224(MessageDigestAlgorithms.SHA3_224, "sha3-224", HASH),
	SHA3_256(MessageDigestAlgorithms.SHA3_256, "sha3-256", HASH),
	SHA3_384(MessageDigestAlgorithms.SHA3_384, "sha3-384", HASH),
	SHA3_512(MessageDigestAlgorithms.SHA3_512, "sha3-512", HASH),
	RIPEMD_128("RIPEMD-128", HASH, "RIPEMD128"),
	RIPEMD_160("RIPEMD-160", HASH, "RIPEMD160"),
	RIPEMD_256("RIPEMD-256", HASH, "RIPEMD256"),
	RIPEMD_320("RIPEMD-320", HASH, "RIPEMD320"),
	TIGER("Tiger", HASH),
	WHIRLPOOL("Whirlpool", HASH);

	private static final Future<Void> bouncyCastleInitialization = CompletableFuture.runAsync(() -> Security.addProvider(new BouncyCastleProvider()), runnable -> {
		final Thread backgroundThread = new Thread(runnable);
		backgroundThread.setDaemon(true);
		backgroundThread.setPriority(MIN_PRIORITY);
		backgroundThread.start();
	});

	private final String name;
	private final String fileExtension;
	private final AlgorithmType type;
	private final String[] aliases;

	private CodecAlgorithm(final String name, final String fileExtension, final AlgorithmType type, final String... aliases) {
		this.name = name;
		this.fileExtension = fileExtension;
		this.type = type;
		this.aliases = aliases;
	}

	private CodecAlgorithm(final String name, final AlgorithmType type, final String... aliases) {
		this(name, name.toLowerCase(Locale.ROOT).replaceAll("[^0-9a-z]", ""), type, aliases);
	}

	public Set<String> getAliases() {
		return new TreeSet<>(Arrays.asList(aliases));
	}

	public Set<CodecMode> getModes() {
		return ENCODING.equals(type) ? EnumSet.allOf(CodecMode.class) : EnumSet.of(ENCODE);
	}

	public static String[] getNames() {
		return Arrays.stream(values()).map(CodecAlgorithm::getName).toArray(String[]::new);
	}

	public DigestUtils createDigestUtils() throws NoSuchAlgorithmException {
		if (!HASH.equals(type)) {
			throw new UnsupportedOperationException("Cannot create " + DigestUtils.class.getName() + " for " + this);
		}

		// Ensure BouncyCastle is fully initialized
		try {
			bouncyCastleInitialization.get();
		}
		catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
		catch (final ExecutionException e) {
			throw new IllegalStateException(e);
		}

		// Create DigestUtils object
		try {
			return new DigestUtils(name);
		}
		catch (final RuntimeException e1) {
			log.log(Level.FINE, name, e1);
			for (final String alias : aliases) {
				try {
					return new DigestUtils(alias);
				}
				catch (final RuntimeException e2) {
					log.log(Level.FINE, alias, e2);
				}
			}
		}
		throw new NoSuchAlgorithmException(name);
	}

}
