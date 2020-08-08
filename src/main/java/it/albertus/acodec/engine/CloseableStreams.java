package it.albertus.acodec.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;

import it.albertus.util.logging.LoggerFactory;

public class CloseableStreams implements Closeable {

	private static final Logger logger = LoggerFactory.getLogger(CloseableStreams.class);

	private final LinkedList<InputStream> inputStreams;
	private final LinkedList<OutputStream> outputStreams;
	private final CountingInputStream countingInputStream;

	public CloseableStreams(final Path input, final Path output) throws IOException {
		inputStreams = createInputStreams(input);
		outputStreams = createOutputStreams(output);
		countingInputStream = new CountingInputStream(inputStreams.getLast());
		inputStreams.add(countingInputStream);
	}

	private static LinkedList<InputStream> createInputStreams(final Path input) throws IOException {
		final LinkedList<InputStream> list = new LinkedList<>();
		list.add(Files.newInputStream(input));
		list.add(new BufferedInputStream(list.getLast()));
		return list;
	}

	private static LinkedList<OutputStream> createOutputStreams(final Path output) throws IOException {
		final LinkedList<OutputStream> list = new LinkedList<>();
		list.add(Files.newOutputStream(output));
		list.add(new BufferedOutputStream(list.getLast()));
		return list;
	}

	@Override
	public synchronized void close() {
		closeStreams(outputStreams);
		closeStreams(inputStreams);
	}

	private static void closeStreams(final LinkedList<? extends Closeable> streams) {
		final Iterator<? extends Closeable> iterator = streams.descendingIterator();
		while (iterator.hasNext()) {
			final Closeable closeable = iterator.next();
			IOUtils.closeQuietly(closeable, e -> logger.log(Level.WARNING, e, () -> "Cannot close " + closeable + ':'));
		}
		streams.clear();
	}

	public LinkedList<InputStream> getInputStreams() { // NOSONAR
		return inputStreams;
	}

	public LinkedList<OutputStream> getOutputStreams() { // NOSONAR
		return outputStreams;
	}

	public long getBytesRead() {
		return countingInputStream.getByteCount();
	}

}
