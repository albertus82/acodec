package io.github.albertus82.acodec.common.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.openjpa.lib.util.Base16Encoder;

import it.albertus.util.NewLine;

enum Base16 implements BaseNCodec {

	INSTANCE;

	static Base16 getCodec() {
		return INSTANCE;
	}

	@Override
	public String encode(final byte[] byteArray) {
		return Base16Encoder.encode(byteArray);
	}

	@Override
	public byte[] decode(final String encoded) {
		final String cleaned = encoded.replace(NewLine.CR.toString(), "").replace(NewLine.LF.toString(), "");
		if (cleaned.matches("[0123456789ABCDEFabcdef]*")) {
			return Base16Encoder.decode(cleaned);
		}
		else {
			throw new IllegalArgumentException(encoded);
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
			final byte[] toWrite = Base16Encoder.encode(count == bufferSize ? buffer : Arrays.copyOfRange(buffer, 0, count)).getBytes(StandardCharsets.US_ASCII);
			output.write(toWrite);
			output.write(System.lineSeparator().getBytes(StandardCharsets.US_ASCII));
		}
		output.flush();
	}

	static void decode(final InputStream input, final OutputStream output) throws IOException {
		final int bufferSize = 2 * 4096;
		final byte[] buffer = new byte[bufferSize];
		int count;
		while ((count = input.read(buffer)) != -1) {
			output.write(Base16Encoder.decode(new String(count == bufferSize ? buffer : Arrays.copyOfRange(buffer, 0, count)).replace(NewLine.CR.toString(), "").replace(NewLine.LF.toString(), "")));
		}
		output.flush();
	}

}
