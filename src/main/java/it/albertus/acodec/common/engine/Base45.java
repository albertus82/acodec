package it.albertus.acodec.common.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base45 {

	static String encode(final byte[] byteArray) {
		return nl.minvws.encoding.Base45.getEncoder().encodeToString(byteArray);
	}

	static byte[] decode(final String encoded) {
		return nl.minvws.encoding.Base45.getDecoder().decode(encoded.replace("\r", "").replace("\n", ""));
	}

}
