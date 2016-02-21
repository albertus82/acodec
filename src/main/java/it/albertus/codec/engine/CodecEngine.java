package it.albertus.codec.engine;

import org.apache.commons.codec.binary.Base64;

public class CodecEngine {

	private CodecType codec;
	private CodecMode mode = CodecMode.ENCODE;

	public String run(String input) {
		if (codec != null) {
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
			return null;
		}
	}

	private String decode(String input) {
		switch (codec) {
		case BASE64:
			return new String(Base64.decodeBase64(input));
		default:
			throw new IllegalStateException("Invalid codec");
		}
	}

	private String encode(String input) {
		switch (codec) {
		case BASE64:
			return Base64.encodeBase64String(input.getBytes());
		default:
			throw new IllegalStateException("Invalid codec");
		}
	}

	public CodecType getCodec() {
		return codec;
	}

	public void setCodec(CodecType codec) {
		this.codec = codec;
	}

	public CodecMode getMode() {
		return mode;
	}

	public void setMode(CodecMode mode) {
		this.mode = mode;
	}

}
