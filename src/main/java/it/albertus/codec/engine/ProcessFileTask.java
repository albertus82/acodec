package it.albertus.codec.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base32InputStream;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.codec.binary.BaseNCodecOutputStream;
import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.io.IOUtils;
import org.freehep.util.io.ASCII85OutputStream;

import it.albertus.codec.resources.Messages;
import it.albertus.util.CRC16OutputStream;
import it.albertus.util.ChecksumOutputStream;
import it.albertus.util.logging.LoggerFactory;
import net.sourceforge.base91.b91cli;

public class ProcessFileTask implements Cancelable {

	private static final Logger logger = LoggerFactory.getLogger(ProcessFileTask.class);

	private static final int BASE_N_LINE_LENGTH = 79;

	private final CodecEngine engine;
	private final File inputFile;
	private final File outputFile;

	private CloseableStreams streams;

	public ProcessFileTask(final CodecEngine engine, final File inputFile, final File outputFile) {
		this.engine = engine;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public String run(final BooleanSupplier canceled) {
		if (engine.getAlgorithm() == null) {
			throw new IllegalStateException(Messages.get("msg.missing.algorithm"));
		}
		if (inputFile == null || !inputFile.isFile()) {
			throw new IllegalStateException(Messages.get("msg.missing.input"));
		}
		switch (engine.getMode()) {
		case DECODE:
			return decode(canceled);
		case ENCODE:
			return encode(canceled);
		default:
			throw new UnsupportedOperationException(Messages.get("err.invalid.mode", engine.getMode()));
		}
	}

	@Override
	public void cancel() {
		if (streams != null) {
			streams.close();
		}
	}

	private String encode(final BooleanSupplier canceled) {
		String value = null;
		final String fileName;
		try {
			if (inputFile.getParentFile().getCanonicalPath().equals(outputFile.getParentFile().getCanonicalPath())) {
				fileName = inputFile.getName();
			}
			else {
				fileName = inputFile.getCanonicalPath();
			}
			try (final CloseableStreams cs = createStreams()) {
				switch (engine.getAlgorithm()) {
				case BASE16:
					Base16.encode(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case BASE32:
					cs.getOutputStreams().add(new BaseNCodecOutputStream(cs.getOutputStreams().getLast(), new Base32(BASE_N_LINE_LENGTH), true));
					IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case BASE64:
					cs.getOutputStreams().add(new Base64OutputStream(cs.getOutputStreams().getLast()));
					IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case ASCII85:
					cs.getOutputStreams().add(new ASCII85OutputStream(cs.getOutputStreams().getLast()));
					IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case BASE91:
					b91cli.encodeWrap(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case CRC16:
					CRC16OutputStream crc16os = getCRC16OutputStream(cs.getInputStreams().getLast());
					value = String.format("%04x", crc16os.getValue());
					IOUtils.write(value + " *" + fileName, cs.getOutputStreams().getLast(), engine.getCharset());
					break;
				case CRC32:
					ChecksumOutputStream<PureJavaCrc32> crc32os = getCRC32OutputStream(cs.getInputStreams().getLast());
					value = String.format("%08x", crc32os.getValue());
					IOUtils.write(fileName + ' ' + value, cs.getOutputStreams().getLast(), engine.getCharset()); // sfv
					break;
				case ADLER32:
					ChecksumOutputStream<Adler32> adler32os = getAdler32OutputStream(cs.getInputStreams().getLast());
					value = String.format("%08x", adler32os.getValue());
					IOUtils.write(value + " *" + fileName, cs.getOutputStreams().getLast(), engine.getCharset());
					break;
				default:
					value = engine.getAlgorithm().createDigestUtils().digestAsHex(cs.getInputStreams().getLast());
					IOUtils.write(value + " *" + fileName, cs.getOutputStreams().getLast(), engine.getCharset());
					break;
				}
			}
		}
		catch (final Exception e) {
			deleteOutputFile();
			if (!canceled.getAsBoolean()) {
				throw new IllegalStateException(Messages.get("err.cannot.encode", engine.getAlgorithm().getName()), e);
			}
		}
		if (canceled.getAsBoolean()) {
			deleteOutputFile();
			throw new CancellationException(Messages.get("msg.file.process.cancel.message"));
		}
		else {
			return value;
		}
	}

	private String decode(final BooleanSupplier canceled) {
		String value = null;
		try (final CloseableStreams cs = createStreams()) {
			switch (engine.getAlgorithm()) {
			case BASE16:
				Base16.decode(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE32:
				cs.getInputStreams().add(new Base32InputStream(cs.getInputStreams().getLast()));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE64:
				cs.getInputStreams().add(new Base64InputStream(cs.getInputStreams().getLast()));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case ASCII85:
				cs.getInputStreams().add(new Ascii85InputStream(cs.getInputStreams().getLast()));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE91:
				b91cli.decode(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			default:
				throw new UnsupportedOperationException(Messages.get("err.invalid.algorithm", engine.getAlgorithm().getName()));
			}
		}
		catch (final Exception e) {
			deleteOutputFile();
			if (!canceled.getAsBoolean()) {
				throw new IllegalStateException(Messages.get("err.cannot.decode", engine.getAlgorithm().getName()), e);
			}
		}
		if (canceled.getAsBoolean()) {
			deleteOutputFile();
			throw new CancellationException(Messages.get("msg.file.process.cancel.message"));
		}
		else {
			return value;
		}
	}

	private void deleteOutputFile() {
		try {
			Files.deleteIfExists(outputFile.toPath());
		}
		catch (final Exception e) {
			logger.log(Level.WARNING, Messages.get("err.cannot.delete.file", outputFile), e);
			outputFile.deleteOnExit();
		}
	}

	private CloseableStreams createStreams() throws IOException {
		streams = new CloseableStreams(inputFile.toPath(), outputFile.toPath());
		return streams;
	}

	public File getInputFile() {
		return inputFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public long getByteCount() {
		return streams != null ? streams.getBytesRead() : 0;
	}

	private static CRC16OutputStream getCRC16OutputStream(final InputStream is) throws IOException {
		try (final CRC16OutputStream os = new CRC16OutputStream()) {
			IOUtils.copyLarge(is, os);
			return os;
		}
	}

	private static ChecksumOutputStream<PureJavaCrc32> getCRC32OutputStream(final InputStream is) throws IOException {
		try (final ChecksumOutputStream<PureJavaCrc32> os = new ChecksumOutputStream<>(new PureJavaCrc32(), 32)) {
			IOUtils.copyLarge(is, os);
			return os;
		}
	}

	private static ChecksumOutputStream<Adler32> getAdler32OutputStream(final InputStream is) throws IOException {
		try (final ChecksumOutputStream<Adler32> os = new ChecksumOutputStream<>(new Adler32(), 32)) {
			IOUtils.copyLarge(is, os);
			return os;
		}
	}

}
