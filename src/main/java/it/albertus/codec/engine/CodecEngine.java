package it.albertus.codec.engine;

import java.nio.charset.Charset;
import java.security.Security;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import it.albertus.codec.resources.Messages;
import it.albertus.util.CRC16;

public class CodecEngine {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private CodecAlgorithm algorithm;
	private CodecMode mode = CodecMode.ENCODE;
	private Charset charset = Charset.defaultCharset();

	public String run(final String input) {
		if (algorithm == null) {
			throw new IllegalStateException(Messages.get("msg.missing.algorithm"));
		}
		if (input == null || input.isEmpty()) {
			throw new IllegalStateException(Messages.get("msg.missing.input"));
		}
		switch (mode) {
		case DECODE:
			return decode(input);
		case ENCODE:
			return encode(input);
		default:
			throw new UnsupportedOperationException(Messages.get("err.invalid.mode", mode));
		}
	}

	private String encode(final String input) {
		try {
			switch (algorithm) {
			case BASE16:
				return Base16.encode(input.getBytes(charset));
			case BASE32:
				return new Base32().encodeAsString(input.getBytes(charset));
			case BASE64:
				return Base64.encodeBase64String(input.getBytes(charset));
			case ASCII85:
				return Ascii85.encode(input.getBytes(charset));
			case BASE91:
				return Base91.encode(input.getBytes(charset));
			case CRC16:
				final CRC16 crc16 = new CRC16();
				crc16.update(input.getBytes(charset));
				return String.format("%04x", crc16.getValue());
			case CRC32:
				final CRC32 crc32 = new CRC32();
				crc32.update(input.getBytes(charset));
				return String.format("%08x", crc32.getValue());
			default:
				return algorithm.createDigestUtils().digestAsHex(input.getBytes(charset));
			}
		}
		catch (final Exception e) {
			throw new IllegalStateException(Messages.get("err.cannot.encode", algorithm.getName()), e);
		}
	}

	private String decode(final String input) {
		try {
			switch (algorithm) {
			case BASE16:
				return new String(Base16.decode(input), charset);
			case BASE32:
				return new String(new Base32().decode(input), charset);
			case BASE64:
				return new String(Base64.decodeBase64(input), charset);
			case ASCII85:
				return new String(Ascii85.decode(input), charset);
			case BASE91:
				return new String(Base91.decode(input), charset);
			default:
				throw new UnsupportedOperationException(Messages.get("err.invalid.algorithm", algorithm.getName()));
			}
		}
		catch (final Exception e) {
			throw new IllegalStateException(Messages.get("err.cannot.decode", algorithm.getName()), e);
		}
	}

	public CodecAlgorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(final CodecAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public CodecMode getMode() {
		return mode;
	}

	public void setMode(final CodecMode mode) {
		this.mode = mode;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

}
