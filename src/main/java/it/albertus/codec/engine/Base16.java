package it.albertus.codec.engine;

import it.albertus.codec.resources.Resources;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.openjpa.lib.util.Base16Encoder;

public class Base16 {

	private static final CharSequence ALPHABET = "0123456789ABCDEF";

	public static String encode(final byte[] byteArray) {
		return Base16Encoder.encode(byteArray);
	}

	public static byte[] decode(String encoded) {
		encoded = encoded.toUpperCase().replace(NewLine.CRLF.toString(), "");
		if (encoded.matches("[" + ALPHABET + "]*")) {
			return Base16Encoder.decode(encoded);
		}
		else {
			throw new IllegalArgumentException(Resources.get("err.invalid.input"));
		}
	}

	public static void encode(final InputStream input, final OutputStream output) throws IOException {
		int position = 0;
		int readInt;
		while ((readInt = input.read()) != -1) {
			final byte readByte = (byte) readInt;
			final byte[] toWrite = Base16Encoder.encode(new byte[] { readByte }).getBytes();
			if (position + toWrite.length >= 79) {
				output.write(NewLine.CRLF.toString().getBytes());
				position = 0;
			}
			output.write(toWrite);
			position += toWrite.length;
		}
		output.flush();
	}

	public static void decode(final InputStream input, final OutputStream output) throws IOException {
		final int bufferSize = 2 * 4096;
		final byte[] read = new byte[bufferSize];
		int count = 0;
		while ((count = input.read(read)) != -1) {
			output.write(Base16Encoder.decode(new String(count == bufferSize ? read : Arrays.copyOfRange(read, 0, count)).replace(NewLine.CRLF.toString(), "")));
		}
		output.flush();
	}

}
