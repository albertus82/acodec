package it.albertus.codec.engine;

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
		ByteArrayInputStream bais = null;
		ByteArrayOutputStream baos = null;
		ASCII85OutputStream a85os = null;
		try {
			bais = new ByteArrayInputStream(byteArray);
			baos = new ByteArrayOutputStream();
			a85os = new ASCII85OutputStream(baos);
			IOUtils.copy(bais, a85os);
		}
		finally {
			IOUtils.closeQuietly(a85os);
			IOUtils.closeQuietly(baos);
			IOUtils.closeQuietly(bais);
		}
		return baos.toString().replaceAll("[" + NewLine.CRLF.toString() + "]+", "");
	}

	public static byte[] decode(final String encoded) throws IOException {
		ByteArrayInputStream bais = null;
		Ascii85InputStream a85is = null;
		ByteArrayOutputStream baos = null;
		try {
			bais = new ByteArrayInputStream(encoded.getBytes());
			a85is = new Ascii85InputStream(bais);
			baos = new ByteArrayOutputStream();
			IOUtils.copy(a85is, baos);
		}
		finally {
			IOUtils.closeQuietly(baos);
			IOUtils.closeQuietly(a85is);
			IOUtils.closeQuietly(bais);
		}
		return baos.toByteArray();
	}

}
