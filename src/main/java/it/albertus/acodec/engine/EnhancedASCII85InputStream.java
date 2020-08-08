package it.albertus.acodec.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.freehep.util.io.ASCII85InputStream;

import lombok.extern.java.Log;

@Log
public class EnhancedASCII85InputStream extends ASCII85InputStream {

	private final InputStream input;

	public EnhancedASCII85InputStream(final InputStream input) {
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
			log.log(Level.FINE, ioe.getMessage(), ioe);
			try {
				input.reset();
			}
			catch (final IOException e) {
				log.log(Level.FINE, e.getMessage(), e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		input.close();
	}

}
