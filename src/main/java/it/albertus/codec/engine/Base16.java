package it.albertus.codec.engine;

import it.albertus.codec.resources.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.openjpa.lib.util.Base16Encoder;

public class Base16 {

	private static final CharSequence ALPHABET = "0123456789ABCDEF";

	public static String encode(final byte[] byteArray) {
		return Base16Encoder.encode(byteArray);
	}

	public static byte[] decode(String encoded) {
		encoded = encoded.toUpperCase();
		if (encoded.matches("[" + ALPHABET + "]*")) {
			return Base16Encoder.decode(encoded);
		}
		else {
			throw new IllegalArgumentException(Resources.get("err.invalid.input"));
		}
	}

	public static void encode(final InputStream input, final OutputStream output) throws IOException {
		int readInt;
		while ((readInt = input.read()) != -1) {
			final byte readByte = (byte) readInt;
			output.write(Base16Encoder.encode(new byte[] { readByte }).getBytes());
		}
		output.flush();
	}

	public static void decode(final InputStream input, final OutputStream output) throws IOException {
		final byte[] read = new byte[2];
		while (input.read(read) != -1) {
			output.write(Base16Encoder.decode(new String(read)));
		}
		output.flush();
	}

}
