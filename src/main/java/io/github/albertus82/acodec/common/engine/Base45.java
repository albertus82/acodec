package io.github.albertus82.acodec.common.engine;

import static nl.minvws.encoding.Base45.getDecoder;
import static nl.minvws.encoding.Base45.getEncoder;

import java.util.Locale;

import io.github.albertus82.util.NewLine;

enum Base45 implements BaseNCodec {

	INSTANCE;

	static final byte ENCODED_CHUNK_SIZE = 3; // bytes
	static final byte DECODED_CHUNK_SIZE = 2; // bytes

	static Base45 getCodec() {
		return INSTANCE;
	}

	@Override
	public String encode(final byte[] byteArray) {
		return getEncoder().encodeToString(byteArray);
	}

	@Override
	public byte[] decode(final String encoded) {
		return getDecoder().decode(encoded.replace(NewLine.CR.toString(), "").replace(NewLine.LF.toString(), "").toUpperCase(Locale.ROOT));
	}

}
