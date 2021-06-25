package it.albertus.acodec.common.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import net.sourceforge.base91.B91Cli;

enum Base91 implements BaseNCodec {

	INSTANCE;

	static Base91 getCodec() {
		return INSTANCE;
	}

	@Override
	public String encode(final byte[] byteArray) throws IOException {
		final OutputStream baos = new ByteArrayOutputStream();
		try (final InputStream bais = new ByteArrayInputStream(byteArray)) {
			B91Cli.encode(bais, baos);
		}
		return baos.toString().trim();
	}

	@Override
	public byte[] decode(final String encoded) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final InputStream bais = new ByteArrayInputStream(encoded.getBytes(StandardCharsets.US_ASCII))) {
			B91Cli.decode(bais, baos);
		}
		return baos.toByteArray();
	}

}
