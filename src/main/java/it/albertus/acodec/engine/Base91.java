package it.albertus.acodec.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.sourceforge.base91.b91cli;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base91 {

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
