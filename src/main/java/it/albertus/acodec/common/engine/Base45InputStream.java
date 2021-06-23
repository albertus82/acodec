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

	ByteBuffer decBuf;
	ByteBuffer encBuf;

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
		try (final ByteArrayOutputStream encBuf2 = new ByteArrayOutputStream(ENCODED_CHUNK_SIZE)) {
			for (int i = 0; i < ENCODED_CHUNK_SIZE; i++) {
				if (encBuf == null || !encBuf.hasRemaining()) {
					refill();
					if (encBuf == null || !encBuf.hasRemaining()) {
						break;
					}
				}
				final int encByte = Byte.toUnsignedInt(encBuf.get());
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
		if (length < 1) {
			encBuf = ByteBuffer.wrap(new byte[0]);
		}
		else {
			encBuf = ByteBuffer.wrap(buf, 0, length);
		}
	}

	@Override
	public void close() throws IOException {
		wrapped.close();
		decBuf = null;
	}

}
