package it.albertus.acodec.common.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.minvws.encoding.Base45;
import nl.minvws.encoding.Base45.Decoder;

@RequiredArgsConstructor
public class Base45InputStream extends InputStream {

	private static final byte ENCODED_CHUNK_SIZE = 3;

	@NonNull private final InputStream wrapped;

	private final Decoder decoder = Base45.getDecoder();

	ByteBuffer encodedBuffer;
	ByteBuffer decodedBuffer;

	@Override
	public int read() throws IOException {
		if (decodedBuffer == null || !decodedBuffer.hasRemaining()) {
			final byte[] decodedChunk = decodeChunk();
			if (decodedChunk.length == 0) {
				return -1;
			}
			decodedBuffer = ByteBuffer.wrap(decodedChunk);
		}
		return Byte.toUnsignedInt(decodedBuffer.get());
	}

	private byte[] decodeChunk() throws IOException {
		try (final ByteArrayOutputStream encBuf2 = new ByteArrayOutputStream(ENCODED_CHUNK_SIZE)) {
			for (int i = 0; i < ENCODED_CHUNK_SIZE; i++) {
				if (encodedBuffer == null || !encodedBuffer.hasRemaining()) {
					refill();
					if (encodedBuffer == null || !encodedBuffer.hasRemaining()) {
						break;
					}
				}
				final int encByte = Byte.toUnsignedInt(encodedBuffer.get());
				if (encByte == '\n' || encByte == '\r') {
					i--;
					continue;
				}
				encBuf2.write(encByte);
			}
			if (encBuf2.size() == 0) {
				return new byte[] {};
			}
			else {
				return decoder.decode(encBuf2.toByteArray());
			}
		}
	}

	private void refill() throws IOException {
		final byte[] buf = new byte[512];
		final int length = wrapped.read(buf);
		encodedBuffer = ByteBuffer.wrap(buf, 0, Math.max(length, 0));
	}

	@Override
	public void close() throws IOException {
		wrapped.close();
		decodedBuffer = null;
	}

}
