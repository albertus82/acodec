package it.albertus.acodec.common.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import lombok.NonNull;
import nl.minvws.encoding.Base45;
import nl.minvws.encoding.Base45.Decoder;

public class Base45InputStream extends InputStream {

	private static final short BUFFERING_FACTOR = 256;

	private static final byte ENCODED_CHUNK_SIZE = 3;
	private static final byte DECODED_CHUNK_SIZE = 2;

	private final InputStream wrapped;

	private final Decoder decoder = Base45.getDecoder();

	private final ByteBuffer encodedBuffer = ByteBuffer.allocate(ENCODED_CHUNK_SIZE * BUFFERING_FACTOR);
	private final ByteBuffer decodedBuffer = ByteBuffer.allocate(DECODED_CHUNK_SIZE * BUFFERING_FACTOR);

	public Base45InputStream(@NonNull final InputStream wrapped) {
		this.wrapped = wrapped;
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
		try (final ByteArrayOutputStream buf = new ByteArrayOutputStream(ENCODED_CHUNK_SIZE * BUFFERING_FACTOR)) {
			while (buf.size() < ENCODED_CHUNK_SIZE * BUFFERING_FACTOR) {
				refillEncodedBuffer();
				if (!encodedBuffer.hasRemaining()) {
					break;
				}
				final int b = Byte.toUnsignedInt(encodedBuffer.get());
				if (b != '\r' && b != '\n') { // discard CR & LF
					buf.write(b);
				}
			}
			return decoder.decode(buf.toByteArray());
		}
	}

	private void refillEncodedBuffer() throws IOException {
		if (!encodedBuffer.hasRemaining()) {
			encodedBuffer.clear();
			final int length = wrapped.read(encodedBuffer.array());
			encodedBuffer.limit(Math.max(length, 0));
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
		wrapped.close();
	}

}
