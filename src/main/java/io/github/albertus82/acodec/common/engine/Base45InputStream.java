package io.github.albertus82.acodec.common.engine;

import static io.github.albertus82.acodec.common.engine.Base45.DECODED_CHUNK_SIZE;
import static io.github.albertus82.acodec.common.engine.Base45.ENCODED_CHUNK_SIZE;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import it.albertus.util.NewLine;
import lombok.NonNull;
import nl.minvws.encoding.Base45;
import nl.minvws.encoding.Base45.Decoder;

class Base45InputStream extends InputStream {

	private static final short BUFFERING_FACTOR = 256;

	@NonNull
	private final Closeable closeable;
	@NonNull
	private final Reader reader;

	private final Decoder decoder = Base45.getDecoder();

	private final CharBuffer encodedBuffer = CharBuffer.allocate(ENCODED_CHUNK_SIZE * BUFFERING_FACTOR);
	private final ByteBuffer decodedBuffer = ByteBuffer.allocate(DECODED_CHUNK_SIZE * BUFFERING_FACTOR);

	public Base45InputStream(@NonNull final InputStream in) {
		closeable = in;
		reader = new InputStreamReader(in, StandardCharsets.ISO_8859_1);
		encodedBuffer.flip();
		decodedBuffer.flip();
	}

	@Override
	public int read() throws IOException {
		refillDecodedBuffer();
		if (!decodedBuffer.hasRemaining()) {
			return -1;
		}
		return Byte.toUnsignedInt(decodedBuffer.get());
	}

	private byte[] decode() throws IOException {
		final int bufferSize = encodedBuffer.array().length;
		try (final ByteArrayOutputStream buf = new ByteArrayOutputStream(bufferSize)) {
			while (buf.size() < bufferSize) {
				refillEncodedBuffer();
				if (!encodedBuffer.hasRemaining()) {
					break;
				}
				final char c = encodedBuffer.get();
				if (NewLine.CRLF.toString().indexOf(c) == -1) { // discard CR & LF
					buf.write(c >= 'a' && c <= 'z' ? c - ('a' - 'A') : c);
				}
			}
			return decoder.decode(buf.toByteArray());
		}
	}

	private void refillEncodedBuffer() throws IOException {
		if (!encodedBuffer.hasRemaining()) {
			encodedBuffer.clear();
			reader.read(encodedBuffer);
			encodedBuffer.flip();
		}
	}

	private void refillDecodedBuffer() throws IOException {
		if (!decodedBuffer.hasRemaining()) {
			decodedBuffer.clear();
			decodedBuffer.put(decode());
			decodedBuffer.flip();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			reader.close();
		}
		finally {
			closeable.close();
		}
	}

}
