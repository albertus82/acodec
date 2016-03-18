package it.albertus.codec.engine;

import it.albertus.codec.resources.Resources;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.mina.proxy.utils.MD4Provider;

public class CodecEngine {

	private static final MD4Provider MD4_PROVIDER = new MD4Provider();

	private CodecAlgorithm algorithm;
	private CodecMode mode = CodecMode.ENCODE;
	private Charset charset = Charset.defaultCharset();

	public String run(final String input) {
		if (input.length() == 0) {
			throw new IllegalStateException(Resources.get("msg.missing.input"));
		}
		if (algorithm != null) {
			switch (mode) {
			case DECODE:
				return decode(input);
			case ENCODE:
				return encode(input);
			default:
				throw new IllegalStateException("Invalid mode");
			}
		}
		else {
			throw new IllegalStateException(Resources.get("msg.missing.algorithm"));
		}
	}

	private String decode(final String input) {
		switch (algorithm) {
		case BASE16:
			return new String(Base16.decode(input), charset);
		case BASE32:
			return new String(new Base32().decode(input), charset);
		case BASE64:
			return new String(Base64.decodeBase64(input), charset);
		default:
			break;
		}
		throw new IllegalStateException(Resources.get("err.cannot.decode", algorithm.getName()));
	}

	private String encode(final String input) {
		switch (algorithm) {
		case BASE16:
			return Base16.encode(input.getBytes(charset));
		case BASE32:
			return new Base32().encodeAsString(input.getBytes(charset));
		case BASE64:
			return Base64.encodeBase64String(input.getBytes(charset));
		case MD2:
			return Hex.encodeHexString(DigestUtils.getMd2Digest().digest(input.getBytes(charset)));
		case MD4:
			try {
				return Hex.encodeHexString(MessageDigest.getInstance(CodecAlgorithm.MD4.getName(), MD4_PROVIDER).digest(input.getBytes(charset)));
			}
			catch (NoSuchAlgorithmException nsae) {
				break;
			}
		case MD5:
			return Hex.encodeHexString(DigestUtils.getMd5Digest().digest(input.getBytes(charset)));
		case SHA1:
			return Hex.encodeHexString(DigestUtils.getSha1Digest().digest(input.getBytes(charset)));
		case SHA256:
			return Hex.encodeHexString(DigestUtils.getSha256Digest().digest(input.getBytes(charset)));
		case SHA384:
			return Hex.encodeHexString(DigestUtils.getSha384Digest().digest(input.getBytes(charset)));
		case SHA512:
			return Hex.encodeHexString(DigestUtils.getSha512Digest().digest(input.getBytes(charset)));
		default:
			break;
		}
		throw new IllegalStateException(Resources.get("err.cannot.encode", algorithm.getName()));
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
