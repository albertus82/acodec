package it.albertus.acodec.common.engine;

import it.albertus.util.NewLine;

enum Base45 implements BaseNCodec {

	INSTANCE;

	static final byte ENCODED_CHUNK_SIZE = 3; // bytes
	static final byte DECODED_CHUNK_SIZE = 2; // bytes

	static Base45 getCodec() {
		return INSTANCE;
	}

	public String encode(final byte[] byteArray) {
		return nl.minvws.encoding.Base45.getEncoder().encodeToString(byteArray);
	}

	public byte[] decode(final String encoded) {
		return nl.minvws.encoding.Base45.getDecoder().decode(encoded.replace(NewLine.CR.toString(), "").replace(NewLine.LF.toString(), ""));
	}

}
