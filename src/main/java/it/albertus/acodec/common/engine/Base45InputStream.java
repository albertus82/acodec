package it.albertus.acodec.common.engine;

import static it.albertus.acodec.common.engine.Base45.DECODED_CHUNK_SIZE;
import static it.albertus.acodec.common.engine.Base45.ENCODED_CHUNK_SIZE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import it.albertus.util.NewLine;
import lombok.NonNull;
import nl.minvws.encoding.Base45;
import nl.minvws.encoding.Base45.Decoder;

class Base45InputStream extends InputStream {

	private static final short BUFFERING_FACTOR = 256;

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
				if (NewLine.CRLF.toString().indexOf(b) == -1) { // discard CR & LF
					buf.write(b >= 'a' && b <= 'z' ? b - ('a' - 'A') : b);
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
