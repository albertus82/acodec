package it.albertus.codec.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sourceforce.base91.b91cli;

import org.apache.commons.io.IOUtils;

public class Base91 {

	public static String encode(final byte[] byteArray) throws IOException {
		final String value;
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			b91cli.encode(inputStream, outputStream);
			outputStream.close();
			inputStream.close();
			value = outputStream.toString();
		}
		catch (final IOException ioe) {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(inputStream);
			throw ioe;
		}
		return value.trim();
	}

	public static byte[] decode(final String encoded) throws IOException {
		final byte[] value;
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(encoded.getBytes());
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			b91cli.decode(inputStream, outputStream);
			outputStream.close();
			inputStream.close();
			value = outputStream.toByteArray();
		}
		catch (final IOException ioe) {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(inputStream);
			throw ioe;
		}
		return value;
	}

}
