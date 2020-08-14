package it.albertus.acodec.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.zip.Adler32;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base32InputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.codec.binary.BaseNCodecInputStream;
import org.apache.commons.codec.binary.BaseNCodecOutputStream;
import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.codec.digest.PureJavaCrc32C;
import org.apache.commons.io.IOUtils;
import org.freehep.util.io.Ascii85InputStream;
import org.freehep.util.io.Ascii85OutputStream;

import it.albertus.acodec.resources.Messages;
import it.albertus.util.CRC16OutputStream;
import it.albertus.util.ChecksumOutputStream;
import it.albertus.util.NewLine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import net.sourceforge.base91.B91Cli;

@Log
@RequiredArgsConstructor
public class ProcessFileTask implements Cancelable {

	private static final int MAX_CHARS_PER_LINE = 76;

	private final CodecConfig config;
	@Getter
	private final File inputFile;
	@Getter
	private final File outputFile;

	private CloseableStreams streams;

	public String run(final BooleanSupplier canceled) {
		if (config.getAlgorithm() == null) {
			throw new IllegalStateException(Messages.get("msg.missing.algorithm"));
		}
		if (inputFile == null || !inputFile.isFile()) {
			throw new IllegalStateException(Messages.get("msg.missing.input"));
		}
		switch (config.getMode()) {
		case DECODE:
			return decode(canceled);
		case ENCODE:
			return encode(canceled);
		default:
			throw new UnsupportedOperationException(Messages.get("err.invalid.mode", config.getMode()));
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
				switch (config.getAlgorithm()) {
				case BASE16:
					Base16.encode(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast(), MAX_CHARS_PER_LINE);
					break;
				case BASE32:
					cs.getOutputStreams().add(new BaseNCodecOutputStream(cs.getOutputStreams().getLast(), new Base32(MAX_CHARS_PER_LINE), true));
					IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case BASE32HEX:
					cs.getOutputStreams().add(new BaseNCodecOutputStream(cs.getOutputStreams().getLast(), new Base32(MAX_CHARS_PER_LINE, NewLine.CRLF.toString().getBytes(StandardCharsets.US_ASCII), true), true));
					IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case BASE64:
					cs.getOutputStreams().add(new Base64OutputStream(cs.getOutputStreams().getLast()));
					IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case BASE64URL:
					cs.getOutputStreams().add(new BaseNCodecOutputStream(cs.getOutputStreams().getLast(), new Base64(true), true));
					IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case ASCII85:
					cs.getOutputStreams().add(new Ascii85OutputStream(cs.getOutputStreams().getLast()));
					IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case BASE91:
					B91Cli.encodeWrap(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
					break;
				case CRC16:
					value = computeCrc16(cs.getInputStreams().getLast());
					IOUtils.write(value + " *" + fileName, cs.getOutputStreams().getLast(), config.getCharset());
					break;
				case CRC32:
					value = computeCrc32(cs.getInputStreams().getLast());
					IOUtils.write(fileName + ' ' + value, cs.getOutputStreams().getLast(), config.getCharset()); // sfv
					break;
				case CRC32C:
					value = computeCrc32C(cs.getInputStreams().getLast());
					IOUtils.write(value + " *" + fileName, cs.getOutputStreams().getLast(), config.getCharset());
					break;
				case ADLER32:
					value = computeAdler32(cs.getInputStreams().getLast());
					IOUtils.write(value + " *" + fileName, cs.getOutputStreams().getLast(), config.getCharset());
					break;
				default:
					value = config.getAlgorithm().createDigestUtils().digestAsHex(cs.getInputStreams().getLast());
					IOUtils.write(value + " *" + fileName, cs.getOutputStreams().getLast(), config.getCharset());
					break;
				}
			}
		}
		catch (final Exception e) {
			deleteOutputFile();
			if (!canceled.getAsBoolean()) {
				throw new IllegalStateException(Messages.get("err.cannot.encode", config.getAlgorithm().getName()), e);
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
			switch (config.getAlgorithm()) {
			case BASE16:
				Base16.decode(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE32:
				cs.getInputStreams().add(new Base32InputStream(cs.getInputStreams().getLast()));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE32HEX:
				cs.getInputStreams().add(new BaseNCodecInputStream(cs.getInputStreams().getLast(), new Base32(true), false) {});
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE64:
			case BASE64URL:
				cs.getInputStreams().add(new Base64InputStream(cs.getInputStreams().getLast()));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case ASCII85:
				cs.getInputStreams().add(new Ascii85InputStream(cs.getInputStreams().getLast()));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE91:
				B91Cli.decode(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			default:
				throw new UnsupportedOperationException(Messages.get("err.invalid.algorithm", config.getAlgorithm().getName()));
			}
		}
		catch (final Exception e) {
			deleteOutputFile();
			if (!canceled.getAsBoolean()) {
				throw new IllegalStateException(Messages.get("err.cannot.decode", config.getAlgorithm().getName()), e);
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
			log.log(Level.WARNING, Messages.get("err.cannot.delete.file", outputFile), e);
			outputFile.deleteOnExit();
		}
	}

	private CloseableStreams createStreams() throws IOException {
		streams = new CloseableStreams(inputFile.toPath(), outputFile.toPath());
		return streams;
	}

	public long getByteCount() {
		return streams != null ? streams.getBytesRead() : 0;
	}

	private static String computeCrc16(final InputStream is) throws IOException {
		try (final CRC16OutputStream os = new CRC16OutputStream()) {
			IOUtils.copyLarge(is, os);
			return os.toString();
		}
	}

	private static String computeCrc32(final InputStream is) throws IOException {
		try (final ChecksumOutputStream<PureJavaCrc32> os = new ChecksumOutputStream<>(new PureJavaCrc32(), 32)) {
			IOUtils.copyLarge(is, os);
			return os.toString();
		}
	}

	private static String computeCrc32C(final InputStream is) throws IOException {
		try (final ChecksumOutputStream<PureJavaCrc32C> os = new ChecksumOutputStream<>(new PureJavaCrc32C(), 32)) {
			IOUtils.copyLarge(is, os);
			return os.toString();
		}
	}

	private static String computeAdler32(final InputStream is) throws IOException {
		try (final ChecksumOutputStream<Adler32> os = new ChecksumOutputStream<>(new Adler32(), 32)) {
			IOUtils.copyLarge(is, os);
			return os.toString();
		}
	}

}
