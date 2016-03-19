package it.albertus.codec.engine;

import java.io.IOException;
import java.io.InputStream;

import org.freehep.util.io.ASCII85InputStream;

public class Ascii85InputStream extends ASCII85InputStream {

	private final InputStream input;

	public Ascii85InputStream(InputStream input) {
		super(input);
		this.input = input;
		discardStartDelimiter();
	}

	private void discardStartDelimiter() {
		input.mark(2);
		try {
			if (!(input.read() == '<' && input.read() == '~')) {
				input.reset();
			}
		}
		catch (IOException ioe) {
			try {
				input.reset();
			}
			catch (IOException e) {}
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		input.close();
	}

}
