package it.albertus.acodec.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.freehep.util.io.ASCII85OutputStream;

import it.albertus.util.NewLine;

public class Ascii85 {

	private Ascii85() {
		throw new IllegalAccessError();
	}

	public static String encode(final byte[] byteArray) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(byteArray); final ASCII85OutputStream a85os = new ASCII85OutputStream(baos)) {
			IOUtils.copy(bais, a85os);
		}
		return baos.toString().replaceAll("[" + NewLine.CRLF.toString() + "]+", "");
	}

	public static byte[] decode(final String encoded) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(encoded.getBytes()); final Ascii85InputStream a85is = new Ascii85InputStream(bais)) {
			IOUtils.copy(a85is, baos);
		}
		return baos.toByteArray();
	}

}
