package it.albertus.codec.engine;

import it.albertus.codec.resources.Resources;

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

}
