package it.albertus.codec.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.io.IOUtils;

import net.sourceforge.base91.b91cli;

public class Base91 {

	private Base91() {
		throw new IllegalAccessError();
	}

	public static String encode(final byte[] byteArray) {
		ByteArrayInputStream bais = null;
		ByteArrayOutputStream baos = null;
		try {
			bais = new ByteArrayInputStream(byteArray);
			baos = new ByteArrayOutputStream();
			b91cli.encode(bais, baos);
		}
		finally {
			IOUtils.closeQuietly(baos, bais);
		}
		return baos.toString().trim();
	}

	public static byte[] decode(final String encoded) {
		ByteArrayInputStream bais = null;
		ByteArrayOutputStream baos = null;
		try {
			bais = new ByteArrayInputStream(encoded.getBytes());
			baos = new ByteArrayOutputStream();
			b91cli.decode(bais, baos);
		}
		finally {
			IOUtils.closeQuietly(baos, bais);
		}
		return baos.toByteArray();
	}

}
