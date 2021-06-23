package it.albertus.acodec.common.engine;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import it.albertus.util.NewLine;
import lombok.NonNull;
import nl.minvws.encoding.Base45;
import nl.minvws.encoding.Base45.Encoder;

public class Base45OutputStream extends FilterOutputStream {

	private final Encoder encoder = Base45.getEncoder();

	private byte len = 0;

	private Integer b0;

	public Base45OutputStream(@NonNull final OutputStream out) {
		super(out);
	}

	@Override
	public void write(final int b) throws IOException {
		if (b0 != null) {
			final byte[] encodedTuple = encoder.encode(new byte[] { b0.byteValue(), (byte) b });
			if (len + encodedTuple.length > 76) {
				out.write(NewLine.CRLF.toString().getBytes(StandardCharsets.US_ASCII));
				len = 0;
			}
			out.write(encodedTuple);
			len += encodedTuple.length;
			b0 = null;
		}
		else {
			b0 = b;
		}
	}

	@Override
	public void close() throws IOException {
		if (b0 != null) {
			out.write(encoder.encode(new byte[] { b0.byteValue() }));
		}
		super.close();
	}

}
