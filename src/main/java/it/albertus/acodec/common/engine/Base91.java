package it.albertus.acodec.common.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.sourceforge.base91.B91Cli;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Base91 {

	static String encode(final byte[] byteArray) throws IOException {
		final OutputStream baos = new ByteArrayOutputStream();
		try (final InputStream bais = new ByteArrayInputStream(byteArray)) {
			B91Cli.encode(bais, baos);
		}
		return baos.toString().trim();
	}

	static byte[] decode(final String encoded) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final InputStream bais = new ByteArrayInputStream(encoded.getBytes())) {
			B91Cli.decode(bais, baos);
		}
		return baos.toByteArray();
	}

}
