package it.albertus.acodec.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.freehep.util.io.ASCII85InputStream;

import it.albertus.util.logging.LoggerFactory;

public class Ascii85InputStream extends ASCII85InputStream {

	private static final Logger logger = LoggerFactory.getLogger(Ascii85InputStream.class);

	private final InputStream input;

	public Ascii85InputStream(final InputStream input) {
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
		catch (final IOException ioe) {
			logger.log(Level.FINE, ioe.getMessage(), ioe);
			try {
				input.reset();
			}
			catch (final IOException e) {
				logger.log(Level.FINE, e.getMessage(), e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		input.close();
	}

}
