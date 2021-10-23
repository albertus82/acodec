package io.github.albertus82.acodec.common.engine;

import static io.github.albertus82.acodec.common.engine.CodecMode.DECODE;
import static io.github.albertus82.acodec.common.engine.CodecMode.ENCODE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.zip.Adler32;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
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

import it.albertus.util.CRC16OutputStream;
import it.albertus.util.ChecksumOutputStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import net.sourceforge.base91.B91Cli;

@Log
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessFileTask implements Cancelable {

	private static final byte MAX_CHARS_PER_LINE = 76;

	@NonNull
	private final CodecConfig config;
	@Getter
	private final File inputFile;
	private final String inputString;
	@Getter
	private final File outputFile;

	private CloseableStreams streams;

	public ProcessFileTask(@NonNull final CodecConfig config, @NonNull final File inputFile) {
		this(config, inputFile, null, null);
	}

	public ProcessFileTask(@NonNull final CodecConfig config, @NonNull final File inputFile, @NonNull final File outputFile) {
		this(config, inputFile, null, outputFile);
	}

	public ProcessFileTask(@NonNull final CodecConfig config, @NonNull final String inputString, @NonNull final File outputFile) {
		this(config, null, inputString, outputFile);
	}

	public Optional<String> run(@NonNull final BooleanSupplier canceled) throws FileNotFoundException, EncoderException, DecoderException {
		if (inputFile != null && !inputFile.isFile()) {
			throw new FileNotFoundException(inputFile.toString());
		}
		switch (config.getMode()) {
		case DECODE:
			decode(canceled);
			return Optional.empty();
		case ENCODE:
			return Optional.ofNullable(encode(canceled));
		default:
			throw new UnsupportedOperationException("Invalid mode: " + config.getMode());
		}
	}

	@Override
	public void cancel() {
		if (streams != null) {
			streams.close();
		}
	}

	private String encode(@NonNull final BooleanSupplier canceled) throws EncoderException {
		if (!config.getAlgorithm().getModes().contains(ENCODE)) {
			throw new EncoderException("The algorithm " + config.getAlgorithm() + " does not support the " + config.getMode() + " operation");
		}
		String value = null;
		try (final CloseableStreams cs = createStreams()) {
			switch (config.getAlgorithm()) {
			case BASE16:
				Base16.encode(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast(), MAX_CHARS_PER_LINE);
				break;
			case BASE32:
				cs.getOutputStreams().add(new BaseNCodecOutputStream(cs.getOutputStreams().getLast(), new Base32(MAX_CHARS_PER_LINE, System.lineSeparator().getBytes(StandardCharsets.US_ASCII)), true));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE32HEX:
				cs.getOutputStreams().add(new BaseNCodecOutputStream(cs.getOutputStreams().getLast(), new Base32(MAX_CHARS_PER_LINE, System.lineSeparator().getBytes(StandardCharsets.US_ASCII), true), true));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE45:
				cs.getOutputStreams().add(new Base45OutputStream(cs.getOutputStreams().getLast()));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE64:
				cs.getOutputStreams().add(new Base64OutputStream(cs.getOutputStreams().getLast(), true, MAX_CHARS_PER_LINE, System.lineSeparator().getBytes(StandardCharsets.US_ASCII)));
				IOUtils.copyLarge(cs.getInputStreams().getLast(), cs.getOutputStreams().getLast());
				break;
			case BASE64URL:
				cs.getOutputStreams().add(new BaseNCodecOutputStream(cs.getOutputStreams().getLast(), new Base64(MAX_CHARS_PER_LINE, System.lineSeparator().getBytes(StandardCharsets.US_ASCII), true), true));
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
				IOUtils.write(buildHashFileContent(value), cs.getOutputStreams().getLast(), config.getCharset());
				break;
			case CRC32:
				value = computeCrc32(cs.getInputStreams().getLast());
				IOUtils.write(buildHashFileContent(value), cs.getOutputStreams().getLast(), config.getCharset());
				break;
			case CRC32C:
				value = computeCrc32C(cs.getInputStreams().getLast());
				IOUtils.write(buildHashFileContent(value), cs.getOutputStreams().getLast(), config.getCharset());
				break;
			case ADLER32:
				value = computeAdler32(cs.getInputStreams().getLast());
				IOUtils.write(buildHashFileContent(value), cs.getOutputStreams().getLast(), config.getCharset());
				break;
			default:
				value = config.getAlgorithm().createDigestUtils().digestAsHex(cs.getInputStreams().getLast());
				IOUtils.write(buildHashFileContent(value), cs.getOutputStreams().getLast(), config.getCharset());
				break;
			}
		}
		catch (final Exception e) {
			deleteOutputFile();
			if (!canceled.getAsBoolean()) {
				throw new EncoderException("An error occurred while encoding " + config.getAlgorithm(), e);
			}
		}
		if (canceled.getAsBoolean()) {
			deleteOutputFile();
			throw new CancellationException("Operation canceled");
		}
		else {
			return value;
		}
	}

	private void decode(@NonNull final BooleanSupplier canceled) throws DecoderException {
		if (!config.getAlgorithm().getModes().contains(DECODE)) {
			throw new DecoderException("The algorithm " + config.getAlgorithm() + " does not support the " + config.getMode() + " operation");
		}
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
			case BASE45:
				cs.getInputStreams().add(new Base45InputStream(cs.getInputStreams().getLast()));
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
				throw new UnsupportedOperationException("Invalid algorithm: " + config.getAlgorithm());
			}
		}
		catch (final Exception e) {
			deleteOutputFile();
			if (!canceled.getAsBoolean()) {
				throw new DecoderException("An error occurred while decoding " + config.getAlgorithm(), e);
			}
		}
		if (canceled.getAsBoolean()) {
			deleteOutputFile();
			throw new CancellationException("Operation canceled");
		}
	}

	private void deleteOutputFile() {
		if (outputFile != null) {
			try {
				Files.deleteIfExists(outputFile.toPath());
			}
			catch (final Exception e) {
				log.log(Level.WARNING, e, () -> "Cannot delete file '" + outputFile + "':");
				outputFile.deleteOnExit();
			}
		}
	}

	private CloseableStreams createStreams() throws IOException {
		if (inputString != null) {
			streams = new CloseableStreams(inputString, config.getCharset(), outputFile != null ? outputFile.toPath() : null);
		}
		else {
			streams = new CloseableStreams(inputFile.toPath(), outputFile != null ? outputFile.toPath() : null);
		}
		return streams;
	}

	public long getByteCount() {
		return streams != null ? streams.getBytesRead() : 0;
	}

	private String buildHashFileContent(@NonNull final String hash) throws IOException {
		final StringBuilder content = new StringBuilder();
		if (inputFile == null) {
			content.append(hash);
		}
		else if ("sfv".equalsIgnoreCase(config.getAlgorithm().getFileExtension())) {
			if (outputFile != null) {
				content.append(buildFileName(inputFile, outputFile)).append(' ');
			}
			content.append(hash);
		}
		else {
			content.append(hash);
			if (outputFile != null) {
				content.append(" *").append(buildFileName(inputFile, outputFile));
			}
		}
		content.append(System.lineSeparator());
		return content.toString();
	}

	private static String buildFileName(@NonNull final File inputFile, @NonNull final File outputFile) throws IOException {
		final File inputCanonicalFile = inputFile.getCanonicalFile();
		final File outputCanonicalFile = outputFile.getCanonicalFile();
		if (inputCanonicalFile.getParentFile() == null && outputCanonicalFile.getParentFile() == null || inputCanonicalFile.getParentFile() != null && inputCanonicalFile.getParentFile().equals(outputCanonicalFile.getParentFile())) {
			return inputFile.getName();
		}
		else {
			return inputFile.getCanonicalPath();
		}
	}

	private static String computeCrc16(@NonNull final InputStream is) throws IOException {
		try (final CRC16OutputStream os = new CRC16OutputStream()) {
			IOUtils.copyLarge(is, os);
			return os.toString();
		}
	}

	private static String computeCrc32(@NonNull final InputStream is) throws IOException {
		try (final ChecksumOutputStream<PureJavaCrc32> os = new ChecksumOutputStream<>(new PureJavaCrc32(), 32)) {
			IOUtils.copyLarge(is, os);
			return os.toString();
		}
	}

	private static String computeCrc32C(@NonNull final InputStream is) throws IOException {
		try (final ChecksumOutputStream<PureJavaCrc32C> os = new ChecksumOutputStream<>(new PureJavaCrc32C(), 32)) {
			IOUtils.copyLarge(is, os);
			return os.toString();
		}
	}

	private static String computeAdler32(@NonNull final InputStream is) throws IOException {
		try (final ChecksumOutputStream<Adler32> os = new ChecksumOutputStream<>(new Adler32(), 32)) {
			IOUtils.copyLarge(is, os);
			return os.toString();
		}
	}

}
