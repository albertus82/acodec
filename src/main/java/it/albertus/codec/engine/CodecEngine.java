package it.albertus.codec.engine;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class CodecEngine {

	private CodecAlgorithm algorithm;
	private CodecMode mode = CodecMode.ENCODE;

	public String run(String input) {
		if (input.length() == 0) {
			throw new IllegalStateException("Write some text in input");
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
			throw new IllegalStateException("Select algoritm");
		}
	}

	private String decode(String input) {
		switch (algorithm) {
		case BASE32:
			return new String(new Base32().decode(input));
		case BASE64:
			return new String(Base64.decodeBase64(input));
		default:
			break;
		}
		throw new IllegalStateException("Cannot decode " + algorithm.getName());
	}

	private String encode(String input) {
		switch (algorithm) {
		case BASE32:
			return new Base32().encodeAsString(input.getBytes());
		case BASE64:
			return Base64.encodeBase64String(input.getBytes());
		case MD2:
			return DigestUtils.md2Hex(input);
		case MD5:
			return DigestUtils.md5Hex(input);
		case SHA1:
			return DigestUtils.sha1Hex(input);
		case SHA256:
			return DigestUtils.sha256Hex(input);
		case SHA384:
			return DigestUtils.sha384Hex(input);
		case SHA512:
			return DigestUtils.sha512Hex(input);
		default:
			break;
		}
		throw new IllegalStateException("Cannot encode " + algorithm.getName());
	}

	public CodecAlgorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(CodecAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public CodecMode getMode() {
		return mode;
	}

	public void setMode(CodecMode mode) {
		this.mode = mode;
	}

}
