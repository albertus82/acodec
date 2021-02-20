package it.albertus.acodec.common.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.openjpa.lib.util.Base16Encoder;

import it.albertus.acodec.common.resources.CommonMessages;
import it.albertus.acodec.common.resources.Messages;
import it.albertus.util.NewLine;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Base16 {

	private static final Messages messages = CommonMessages.INSTANCE;

	static String encode(final byte[] byteArray) {
		return Base16Encoder.encode(byteArray);
	}

	static byte[] decode(final String encoded) {
		final String cleaned = encoded.replace(NewLine.CRLF.toString(), "");
		if (cleaned.matches("[0123456789ABCDEFabcdef]*")) {
			return Base16Encoder.decode(cleaned);
		}
		else {
			throw new IllegalArgumentException(messages.get("common.err.invalid.input"));
		}
	}

	static void encode(final InputStream input, final OutputStream output, final int maxCharsPerLine) throws IOException {
		if (maxCharsPerLine < 1) {
			throw new IllegalArgumentException(Integer.toString(maxCharsPerLine));
		}
		final int bufferSize = maxCharsPerLine / 2;
		final byte[] buffer = new byte[bufferSize];
		int count;
		while ((count = input.read(buffer)) != -1) {
			final byte[] toWrite = Base16Encoder.encode(count == bufferSize ? buffer : Arrays.copyOfRange(buffer, 0, count)).getBytes();
			output.write(toWrite);
			output.write(NewLine.CRLF.toString().getBytes());
		}
		output.flush();
	}

	static void decode(final InputStream input, final OutputStream output) throws IOException {
		final int bufferSize = 2 * 4096;
		final byte[] buffer = new byte[bufferSize];
		int count;
		while ((count = input.read(buffer)) != -1) {
			output.write(Base16Encoder.decode(new String(count == bufferSize ? buffer : Arrays.copyOfRange(buffer, 0, count)).replace(NewLine.CRLF.toString(), "")));
		}
		output.flush();
	}

}
