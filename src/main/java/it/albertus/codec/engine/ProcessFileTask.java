package it.albertus.codec.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base32InputStream;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.codec.binary.BaseNCodecOutputStream;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.freehep.util.io.ASCII85OutputStream;

import it.albertus.codec.resources.Messages;
import it.albertus.util.CRC16OutputStream;
import it.albertus.util.CRC32OutputStream;
import it.albertus.util.ISupplier;
import it.albertus.util.logging.LoggerFactory;
import net.sourceforge.base91.b91cli;

public class ProcessFileTask implements Cancelable {

	private static final Logger logger = LoggerFactory.getLogger(ProcessFileTask.class);

	private final LinkedList<InputStream> inputStreams = new LinkedList<InputStream>();
	private final LinkedList<OutputStream> outputStreams = new LinkedList<OutputStream>();

	private CountingInputStream cis;

	private final CodecEngine engine;
	private final File inputFile;
	private final File outputFile;

	public ProcessFileTask(final CodecEngine engine, final File inputFile, final File outputFile) {
		this.engine = engine;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	public String run(final ISupplier<Boolean> canceled) throws CancelException {
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
		closeStreams();
	}

	private String encode(final ISupplier<Boolean> canceled) throws CancelException {
		String value = null;
		final String fileName;
		try {
			if (inputFile.getParentFile().getCanonicalPath().equals(outputFile.getParentFile().getCanonicalPath())) {
				fileName = inputFile.getName();
			}
			else {
				fileName = inputFile.getCanonicalPath();
			}
			createStreams();
			switch (engine.getAlgorithm()) {
			case BASE16:
				Base16.encode(inputStreams.getLast(), outputStreams.getLast());
				break;
			case BASE32:
				outputStreams.add(new BaseNCodecOutputStream(outputStreams.getLast(), new Base32(79), true));
				IOUtils.copyLarge(inputStreams.getLast(), outputStreams.getLast());
				break;
			case BASE64:
				outputStreams.add(new Base64OutputStream(outputStreams.getLast()));
				IOUtils.copyLarge(inputStreams.getLast(), outputStreams.getLast());
				break;
			case ASCII85:
				outputStreams.add(new ASCII85OutputStream(outputStreams.getLast()));
				IOUtils.copyLarge(inputStreams.getLast(), outputStreams.getLast());
				break;
			case BASE91:
				b91cli.encodeWrap(inputStreams.getLast(), outputStreams.getLast());
				break;
			case CRC16:
				CRC16OutputStream crcos = null;
				try {
					crcos = new CRC16OutputStream();
					IOUtils.copyLarge(inputStreams.getLast(), crcos);
				}
				finally {
					if (crcos != null) {
						crcos.close();
					}
				}
				value = String.format("%04x", crcos.getValue());
				IOUtils.write(value + " *" + fileName, outputStreams.getLast(), engine.getCharset());
				break;
			case CRC32:
				CRC32OutputStream c32os = null;
				try {
					c32os = new CRC32OutputStream();
					IOUtils.copyLarge(inputStreams.getLast(), c32os);
				}
				finally {
					if (c32os != null) {
						c32os.close();
					}
				}
				value = String.format("%08x", c32os.getValue());
				IOUtils.write(fileName + ' ' + value, outputStreams.getLast(), engine.getCharset()); // sfv
				break;
			case MD2:
				value = DigestUtils.md2Hex(inputStreams.getLast());
				IOUtils.write(value + " *" + fileName, outputStreams.getLast(), engine.getCharset());
				break;
			case MD4:
				value = Hex.encodeHexString(DigestUtils.updateDigest(MessageDigest.getInstance(CodecAlgorithm.MD4.name(), CodecEngine.getMd4Provider()), inputStreams.getLast()).digest());
				IOUtils.write(value + " *" + fileName, outputStreams.getLast(), engine.getCharset());
				break;
			case MD5:
				value = DigestUtils.md5Hex(inputStreams.getLast());
				IOUtils.write(value + " *" + fileName, outputStreams.getLast(), engine.getCharset());
				break;
			case SHA1:
				value = DigestUtils.sha1Hex(inputStreams.getLast());
				IOUtils.write(value + " *" + fileName, outputStreams.getLast(), engine.getCharset());
				break;
			case SHA256:
				value = DigestUtils.sha256Hex(inputStreams.getLast());
				IOUtils.write(value + " *" + fileName, outputStreams.getLast(), engine.getCharset());
				break;
			case SHA384:
				value = DigestUtils.sha384Hex(inputStreams.getLast());
				IOUtils.write(value + " *" + fileName, outputStreams.getLast(), engine.getCharset());
				break;
			case SHA512:
				value = DigestUtils.sha512Hex(inputStreams.getLast());
				IOUtils.write(value + " *" + fileName, outputStreams.getLast(), engine.getCharset());
				break;
			default:
				throw new UnsupportedOperationException(Messages.get("err.invalid.algorithm", engine.getAlgorithm().getName()));
			}
		}
		catch (final Exception e) {
			deleteOutputFile();
			if (Boolean.FALSE.equals(canceled.get())) {
				throw new IllegalStateException(Messages.get("err.cannot.encode", engine.getAlgorithm().getName()), e);
			}
		}
		finally {
			closeStreams();
		}
		if (Boolean.TRUE.equals(canceled.get())) {
			deleteOutputFile();
			throw new CancelException(Messages.get("msg.file.process.cancel.message"));
		}
		else {
			return value;
		}
	}

	private String decode(final ISupplier<Boolean> canceled) throws CancelException {
		String value = null;
		try {
			createStreams();
			switch (engine.getAlgorithm()) {
			case BASE16:
				Base16.decode(inputStreams.getLast(), outputStreams.getLast());
				break;
			case BASE32:
				inputStreams.add(new Base32InputStream(inputStreams.getLast()));
				IOUtils.copyLarge(inputStreams.getLast(), outputStreams.getLast());
				break;
			case BASE64:
				inputStreams.add(new Base64InputStream(inputStreams.getLast()));
				IOUtils.copyLarge(inputStreams.getLast(), outputStreams.getLast());
				break;
			case ASCII85:
				inputStreams.add(new Ascii85InputStream(inputStreams.getLast()));
				IOUtils.copyLarge(inputStreams.getLast(), outputStreams.getLast());
				break;
			case BASE91:
				b91cli.decode(inputStreams.getLast(), outputStreams.getLast());
				break;
			default:
				throw new UnsupportedOperationException(Messages.get("err.invalid.algorithm", engine.getAlgorithm().getName()));
			}
		}
		catch (final Exception e) {
			deleteOutputFile();
			if (Boolean.FALSE.equals(canceled.get())) {
				throw new IllegalStateException(Messages.get("err.cannot.decode", engine.getAlgorithm().getName()), e);
			}
		}
		finally {
			closeStreams();
		}
		if (Boolean.TRUE.equals(canceled.get())) {
			deleteOutputFile();
			throw new CancelException(Messages.get("msg.file.process.cancel.message"));
		}
		else {
			return value;
		}
	}

	private void deleteOutputFile() {
		closeOutputStreams();
		try {
			if (!outputFile.delete()) {
				outputFile.deleteOnExit();
			}
		}
		catch (final Exception e) {
			logger.log(Level.WARNING, Messages.get("err.cannot.delete.file", outputFile), e);
		}
	}

	private void createStreams() throws FileNotFoundException {
		createInputStreams();
		createOutputStreams();
	}

	private synchronized void createInputStreams() throws FileNotFoundException {
		if (!inputStreams.isEmpty()) {
			throw new IllegalStateException("InputStream collection is not empty!");
		}
		inputStreams.add(new FileInputStream(inputFile));
		inputStreams.add(new BufferedInputStream(inputStreams.getLast()));
		cis = new CountingInputStream(inputStreams.getLast());
		inputStreams.add(cis);
	}

	private synchronized void createOutputStreams() throws FileNotFoundException {
		if (!outputStreams.isEmpty()) {
			throw new IllegalStateException("OutputStream collection is not empty!");
		}
		outputStreams.add(new FileOutputStream(outputFile));
		outputStreams.add(new BufferedOutputStream(outputStreams.getLast()));
	}

	private void closeStreams() {
		closeOutputStreams();
		closeInputStreams();
	}

	private synchronized void closeOutputStreams() {
		final Iterator<OutputStream> iterator = outputStreams.descendingIterator();
		while (iterator.hasNext()) {
			IOUtils.closeQuietly(iterator.next());
		}
		outputStreams.clear();
	}

	private synchronized void closeInputStreams() {
		final Iterator<InputStream> iterator = inputStreams.descendingIterator();
		while (iterator.hasNext()) {
			IOUtils.closeQuietly(iterator.next());
		}
		inputStreams.clear();
	}

	public File getInputFile() {
		return inputFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public long getByteCount() {
		return cis != null ? cis.getByteCount() : 0;
	}

}
