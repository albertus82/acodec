package it.albertus.codec.engine;

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

	BASE16("Base16", false),
	BASE32("Base32", false),
	BASE64("Base64", false),
	ASCII85("Ascii85", false),
	BASE91("basE91", false),
	CRC16("CRC-16", true),
	CRC32("CRC-32", "sfv", true),
	MD2(MessageDigestAlgorithms.MD2, true),
	MD4("MD4", true),
	MD5(MessageDigestAlgorithms.MD5, true),
	SHA_1(MessageDigestAlgorithms.SHA_1, true, "sha1"),
	SHA_224(MessageDigestAlgorithms.SHA_224, true, "sha224"),
	SHA_256(MessageDigestAlgorithms.SHA_256, true, "sha256"),
	SHA_384(MessageDigestAlgorithms.SHA_384, true, "sha384"),
	SHA_512(MessageDigestAlgorithms.SHA_512, true, "sha512"),
	SHA_512_224(MessageDigestAlgorithms.SHA_512_224, "sha512-224", true),
	SHA_512_256(MessageDigestAlgorithms.SHA_512_256, "sha512-256", true),
	SHA3_224(MessageDigestAlgorithms.SHA3_224, "sha3-224", true),
	SHA3_256(MessageDigestAlgorithms.SHA3_256, "sha3-256", true),
	SHA3_384(MessageDigestAlgorithms.SHA3_384, "sha3-384", true),
	SHA3_512(MessageDigestAlgorithms.SHA3_512, "sha3-512", true),
	RIPEMD_128("RIPEMD-128", true, "RIPEMD128"),
	RIPEMD_160("RIPEMD-160", true, "RIPEMD160"),
	RIPEMD_256("RIPEMD-256", true, "RIPEMD256"),
	RIPEMD_320("RIPEMD-320", true, "RIPEMD320"),
	WHIRLPOOL("Whirlpool", true);

	private static final Logger logger = LoggerFactory.getLogger(CodecAlgorithm.class);

	private static final Future<Void> bouncyCastleInitialization = CompletableFuture.runAsync(() -> Security.addProvider(new BouncyCastleProvider()), runnable -> {
		final Thread backgroundThread = new Thread(runnable);
		backgroundThread.setDaemon(true);
		backgroundThread.setPriority(Thread.MIN_PRIORITY);
		backgroundThread.start();
	});

	private final String name;
	private final String fileExtension;
	private final boolean digest;
	private final String[] aliases;

	private CodecAlgorithm(final String name, final String fileExtension, final boolean digest, final String... aliases) {
		this.name = name;
		this.fileExtension = fileExtension;
		this.digest = digest;
		this.aliases = aliases;
	}

	private CodecAlgorithm(final String name, final boolean digest, final String... aliases) {
		this(name, name.toLowerCase().replaceAll("[^0-9a-z]", ""), digest, aliases);
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
		return digest ? EnumSet.of(CodecMode.ENCODE) : EnumSet.allOf(CodecMode.class);
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

	public DigestUtils createDigestUtils() throws NoSuchAlgorithmException {
		if (!digest) {
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
