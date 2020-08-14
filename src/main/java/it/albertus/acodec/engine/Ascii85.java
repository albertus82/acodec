package it.albertus.acodec.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.freehep.util.io.Ascii85InputStream;
import org.freehep.util.io.Ascii85OutputStream;

import it.albertus.util.NewLine;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Ascii85 {

	static String encode(final byte[] byteArray) throws IOException {
		final OutputStream baos = new ByteArrayOutputStream();
		try (final InputStream bais = new ByteArrayInputStream(byteArray); final OutputStream a85os = new Ascii85OutputStream(baos)) {
			IOUtils.copy(bais, a85os);
		}
		return baos.toString().replaceAll("[" + NewLine.CRLF.toString() + "]+", "");
	}

	static byte[] decode(final String encoded) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final InputStream bais = new ByteArrayInputStream(encoded.getBytes()); final InputStream a85is = new Ascii85InputStream(bais)) {
			IOUtils.copy(a85is, baos);
		}
		return baos.toByteArray();
	}

}
