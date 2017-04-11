package it.albertus.codec.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sourceforge.base91.b91cli;

public class Base91 {

	private Base91() {
		throw new IllegalAccessError();
	}

	public static String encode(final byte[] byteArray) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray)) {
			b91cli.encode(bais, baos);
		}
		return baos.toString().trim();
	}

	public static byte[] decode(final String encoded) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(encoded.getBytes())) {
			b91cli.decode(bais, baos);
		}
		return baos.toByteArray();
	}

}
