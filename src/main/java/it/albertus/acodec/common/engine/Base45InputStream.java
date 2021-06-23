package it.albertus.acodec.common.engine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.minvws.encoding.Base45;

@RequiredArgsConstructor
public class Base45InputStream extends InputStream {

	private static final byte ENCODED_CHUNK_SIZE = 3;

	@NonNull private final InputStream wrapped;

	ByteBuffer decBuf;

	@Override
	public int read() throws IOException {
		if (decBuf == null || !decBuf.hasRemaining()) {
			final byte[] decodedChunk = decodeChunk();
			if (decodedChunk.length == 0) {
				return -1;
			}
			decBuf = ByteBuffer.wrap(decodedChunk);
		}
		return Byte.toUnsignedInt(decBuf.get());
	}

	private byte[] decodeChunk() throws IOException {
		try (final ByteArrayOutputStream encBuf = new ByteArrayOutputStream(ENCODED_CHUNK_SIZE)) {
			for (int i = 0; i < ENCODED_CHUNK_SIZE; i++) {
				final int encByte = wrapped.read();
				if (encByte == -1) {
					break;
				}
				encBuf.write(encByte);
			}
			if (encBuf.size() == 0) {
				return new byte[] {};
			}
			else {
				return Base45.getDecoder().decode(encBuf.toByteArray());
			}
		}
	}

	@Override
	public void close() throws IOException {
		wrapped.close();
		decBuf = null;
	}

}
