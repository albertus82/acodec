package it.albertus.codec.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.freehep.util.io.ASCII85InputStream;
import org.freehep.util.io.ASCII85OutputStream;

public class Ascii85 {

	public static String encode(final byte[] byteArray) throws IOException {
		final String value;
		final ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final ASCII85OutputStream outputStream = new ASCII85OutputStream(byteArrayOutputStream);
		try {
			IOUtils.copyLarge(inputStream, outputStream);
			outputStream.close();
			inputStream.close();
			value = byteArrayOutputStream.toString();
		}
		catch (final IOException ioe) {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(inputStream);
			throw ioe;
		}
		return value;
	}

	public static byte[] decode(final String encoded) throws IOException {
		final byte[] value;
		final ASCII85InputStream inputStream = new ASCII85InputStream(new ByteArrayInputStream(encoded.getBytes()));
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			IOUtils.copyLarge(inputStream, outputStream);
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
