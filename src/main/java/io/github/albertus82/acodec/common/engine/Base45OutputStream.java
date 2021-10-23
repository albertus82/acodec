package io.github.albertus82.acodec.common.engine;

import static io.github.albertus82.acodec.common.engine.Base45.DECODED_CHUNK_SIZE;
import static io.github.albertus82.acodec.common.engine.Base45.ENCODED_CHUNK_SIZE;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import lombok.NonNull;
import nl.minvws.encoding.Base45;
import nl.minvws.encoding.Base45.Encoder;

class Base45OutputStream extends FilterOutputStream {

	private static final byte MAX_CHARS_PER_LINE = 76;

	private final Encoder encoder = Base45.getEncoder();

	private final ByteBuffer buf = ByteBuffer.allocate(MAX_CHARS_PER_LINE / ENCODED_CHUNK_SIZE * DECODED_CHUNK_SIZE);

	public Base45OutputStream(@NonNull final OutputStream out) {
		super(out);
	}

	@Override
	public void write(final int b) throws IOException {
		buf.put((byte) b);
		if (!buf.hasRemaining()) {
			out.write(buildEncodedLine(buf.array()));
			buf.clear();
		}
	}

	@Override
	public void close() throws IOException {
		if (buf.position() > 0) {
			out.write(buildEncodedLine(Arrays.copyOf(buf.array(), buf.position())));
		}
		super.close();
	}

	private byte[] buildEncodedLine(@NonNull final byte[] src) {
		final byte[] encodedLine = encoder.encode(src);
		final byte[] toWrite = Arrays.copyOf(encodedLine, encodedLine.length + System.lineSeparator().length());
		System.arraycopy(System.lineSeparator().getBytes(StandardCharsets.US_ASCII), 0, toWrite, encodedLine.length, System.lineSeparator().length());
		return toWrite;
	}

}
