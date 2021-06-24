package it.albertus.acodec.common.engine;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import it.albertus.util.NewLine;
import lombok.NonNull;

class Base45OutputStream extends FilterOutputStream {

	private static final byte MAX_CHARS_PER_LINE = 76;

	private static final String NEWLINE = NewLine.CRLF.toString();

	private final nl.minvws.encoding.Base45.Encoder encoder = nl.minvws.encoding.Base45.getEncoder();

	private final ByteBuffer buf = ByteBuffer.allocate(MAX_CHARS_PER_LINE / Base45.ENCODED_CHUNK_SIZE * Base45.DECODED_CHUNK_SIZE);

	public Base45OutputStream(@NonNull final OutputStream out) {
		super(out);
	}

	@Override
	public void write(final int b) throws IOException {
		buf.put((byte) b);
		if (!buf.hasRemaining()) {
			final byte[] encodedLine = encoder.encode(buf.array());
			final byte[] toWrite = Arrays.copyOf(encodedLine, encodedLine.length + NEWLINE.length());
			System.arraycopy(NEWLINE.getBytes(StandardCharsets.US_ASCII), 0, toWrite, encodedLine.length, NEWLINE.length());
			out.write(toWrite);
			buf.clear();
		}
	}

	@Override
	public void close() throws IOException {
		if (buf.position() > 0) {
			out.write(encoder.encode(Arrays.copyOf(buf.array(), buf.position())));
			out.write(NewLine.CRLF.toString().getBytes(StandardCharsets.US_ASCII));
		}
		super.close();
	}

}
