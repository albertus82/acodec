package it.albertus.acodec.common.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import lombok.NonNull;
import nl.minvws.encoding.Base45;
import nl.minvws.encoding.Base45.Decoder;

public class Base45InputStream extends InputStream {

	private static final short READ_BUFFER_SIZE = 512;

	private static final byte ENCODED_CHUNK_SIZE = 3;
	private static final byte DECODED_CHUNK_SIZE = 2;

	private final InputStream wrapped;

	private final Decoder decoder = Base45.getDecoder();

	private final ByteBuffer encodedBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
	private final ByteBuffer decodedBuffer = ByteBuffer.allocate(DECODED_CHUNK_SIZE);

	public Base45InputStream(@NonNull final InputStream wrapped) {
		this.wrapped = wrapped;
		encodedBuffer.flip();
		decodedBuffer.flip();
	}

	@Override
	public int read() throws IOException {
		if (!decodedBuffer.hasRemaining()) {
			refillDecodedBuffer();
			if (!decodedBuffer.hasRemaining()) {
				return -1;
			}
		}
		return Byte.toUnsignedInt(decodedBuffer.get());
	}

	private byte[] decodeChunk() throws IOException {
		try (final ByteArrayOutputStream buf = new ByteArrayOutputStream(ENCODED_CHUNK_SIZE)) {
			while (buf.size() < ENCODED_CHUNK_SIZE) {
				if (!encodedBuffer.hasRemaining()) {
					refillEncodedBuffer();
					if (!encodedBuffer.hasRemaining()) {
						break;
					}
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
		encodedBuffer.clear();
		final int length = wrapped.read(encodedBuffer.array());
		encodedBuffer.limit(Math.max(length, 0));
	}

	private void refillDecodedBuffer() throws IOException {
		decodedBuffer.clear();
		decodedBuffer.put(decodeChunk());
		decodedBuffer.flip();
	}

	@Override
	public void close() throws IOException {
		wrapped.close();
	}

}
