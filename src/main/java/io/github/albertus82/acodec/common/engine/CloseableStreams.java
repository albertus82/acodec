package io.github.albertus82.acodec.common.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
@Getter(AccessLevel.PACKAGE)
class CloseableStreams implements Closeable {

	private final LinkedList<InputStream> inputStreams;
	private final LinkedList<OutputStream> outputStreams;

	@Getter(AccessLevel.NONE)
	private final CountingInputStream countingInputStream;

	CloseableStreams(@NonNull final Path input, final Path output) throws IOException {
		inputStreams = createInputStreams(input);
		outputStreams = createOutputStreams(output);
		countingInputStream = new CountingInputStream(inputStreams.getLast());
		inputStreams.add(countingInputStream);
	}

	CloseableStreams(@NonNull final String input, @NonNull final Charset charset, final Path output) throws IOException {
		inputStreams = createInputStreams(input, charset);
		outputStreams = createOutputStreams(output);
		countingInputStream = new CountingInputStream(inputStreams.getLast());
		inputStreams.add(countingInputStream);
	}

	private static LinkedList<InputStream> createInputStreams(@NonNull final Path input) throws IOException {
		final LinkedList<InputStream> list = new LinkedList<>();
		list.add(Files.newInputStream(input));
		list.add(new BufferedInputStream(list.getLast()));
		return list;
	}

	private static LinkedList<InputStream> createInputStreams(@NonNull final String input, @NonNull final Charset charset) {
		final LinkedList<InputStream> list = new LinkedList<>();
		list.add(new ByteArrayInputStream(input.getBytes(charset)));
		return list;
	}

	private static LinkedList<OutputStream> createOutputStreams(final Path output) throws IOException {
		final LinkedList<OutputStream> list = new LinkedList<>();
		list.add(output == null ? System.out : Files.newOutputStream(output)); // NOSONAR Replace this use of System.out or System.err by a logger. Standard outputs should not be used directly to log anything (java:S106)
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
			IOUtils.closeQuietly(closeable, e -> log.log(Level.FINE, e, () -> "Cannot close " + closeable + ':'));
		}
		streams.clear();
	}

	long getBytesRead() {
		return countingInputStream.getByteCount();
	}

}
