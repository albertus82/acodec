package it.albertus.codec.engine;

import static it.albertus.codec.engine.AlgorithmType.CHECKSUM;
import static it.albertus.codec.engine.AlgorithmType.ENCODING;
import static it.albertus.codec.engine.AlgorithmType.HASH;

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import it.albertus.util.logging.LoggerFactory;

public enum CodecAlgorithm {

	BASE16("Base16", ENCODING),
	BASE32("Base32", ENCODING),
	BASE64("Base64", "b64", ENCODING),
	ASCII85("Ascii85", ENCODING),
	BASE91("basE91", ENCODING),
	CRC16("CRC-16", CHECKSUM),
	CRC32("CRC-32", "sfv", CHECKSUM),
	ADLER32("Adler-32", CHECKSUM),
	MD2(MessageDigestAlgorithms.MD2, HASH),
	MD4("MD4", HASH),
	MD5(MessageDigestAlgorithms.MD5, HASH),
	SHA_1(MessageDigestAlgorithms.SHA_1, HASH, "sha1"),
	SHA_224(MessageDigestAlgorithms.SHA_224, HASH, "sha224"),
	SHA_256(MessageDigestAlgorithms.SHA_256, HASH, "sha256"),
	SHA_384(MessageDigestAlgorithms.SHA_384, HASH, "sha384"),
	SHA_512(MessageDigestAlgorithms.SHA_512, HASH, "sha512"),
	SHA_512_224(MessageDigestAlgorithms.SHA_512_224, "sha512-224", HASH),
	SHA_512_256(MessageDigestAlgorithms.SHA_512_256, "sha512-256", HASH),
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

	private static final Logger logger = LoggerFactory.getLogger(CodecAlgorithm.class);

	private static final Future<Void> bouncyCastleInitialization = CompletableFuture.runAsync(() -> Security.addProvider(new BouncyCastleProvider()), runnable -> {
		final Thread backgroundThread = new Thread(runnable);
		backgroundThread.setDaemon(true);
		backgroundThread.setPriority(Thread.MIN_PRIORITY);
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
		this(name, name.toLowerCase().replaceAll("[^0-9a-z]", ""), type, aliases);
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public Set<String> getAliases() {
		return new TreeSet<>(Arrays.asList(aliases));
	}

	public Set<CodecMode> getModes() {
		return ENCODING.equals(type) ? EnumSet.allOf(CodecMode.class) : EnumSet.of(CodecMode.ENCODE);
	}

	public AlgorithmType getType() {
		return type;
	}

	public static String[] getNames() {
		return Arrays.stream(CodecAlgorithm.values()).map(CodecAlgorithm::getName).toArray(String[]::new);
	}

	public DigestUtils createDigestUtils() throws NoSuchAlgorithmException {
		if (!HASH.equals(type)) {
			throw new UnsupportedOperationException();
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
			logger.log(Level.FINE, name, e1);
			for (final String alias : aliases) {
				try {
					return new DigestUtils(alias);
				}
				catch (final RuntimeException e2) {
					logger.log(Level.FINE, alias, e2);
				}
			}
		}
		throw new NoSuchAlgorithmException(name);
	}

}
