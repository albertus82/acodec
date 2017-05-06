package it.albertus.codec.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base32InputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.codec.binary.BaseNCodecOutputStream;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.mina.proxy.utils.MD4Provider;
import org.freehep.util.io.ASCII85OutputStream;

import it.albertus.codec.resources.Messages;
import it.albertus.util.CRC16;
import it.albertus.util.CRC16OutputStream;
import it.albertus.util.CRC32OutputStream;
import it.albertus.util.logging.LoggerFactory;
import net.sourceforge.base91.b91cli;

public class CodecEngine {

	private static final Logger logger = LoggerFactory.getLogger(CodecEngine.class);

	private static final MD4Provider MD4_PROVIDER = new MD4Provider();

	private CodecAlgorithm algorithm;
	private CodecMode mode = CodecMode.ENCODE;
	private Charset charset = Charset.defaultCharset();

	public String run(final String input) {
		if (algorithm == null) {
			throw new IllegalStateException(Messages.get("msg.missing.algorithm"));
		}
		if (input == null || input.isEmpty()) {
			throw new IllegalStateException(Messages.get("msg.missing.input"));
		}
		switch (mode) {
		case DECODE:
			return decode(input);
		case ENCODE:
			return encode(input);
		default:
			throw new IllegalStateException("Invalid mode");
		}
	}

	public String run(final File inputFile, final File outputFile) {
		if (inputFile == null || !inputFile.exists()) {
			throw new IllegalStateException(Messages.get("msg.missing.input"));
		}
		if (algorithm != null) {
			switch (mode) {
			case DECODE:
				return decode(inputFile, outputFile);
			case ENCODE:
				return encode(inputFile, outputFile);
			default:
				throw new IllegalStateException("Invalid mode");
			}
		}
		else {
			throw new IllegalStateException(Messages.get("msg.missing.algorithm"));
		}
	}

	private String encode(final File inputFile, final File outputFile) {
		String value = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		OutputStream eos = null;
		final String fileName;
		try {
			if (inputFile.getParentFile().getCanonicalPath().equals(outputFile.getParentFile().getCanonicalPath())) {
				fileName = inputFile.getName();
			}
			else {
				fileName = inputFile.getCanonicalPath();
			}
			fis = new FileInputStream(inputFile);
			bis = new BufferedInputStream(fis);
			fos = new FileOutputStream(outputFile);
			switch (algorithm) {
			case BASE16:
				bos = new BufferedOutputStream(fos);
				Base16.encode(bis, bos);
				break;
			case BASE32:
				bos = new BufferedOutputStream(fos);
				eos = new BaseNCodecOutputStream(bos, new Base32(79), true);
				IOUtils.copyLarge(bis, eos);
				break;
			case BASE64:
				bos = new BufferedOutputStream(fos);
				eos = new Base64OutputStream(bos);
				IOUtils.copyLarge(bis, eos);
				break;
			case ASCII85:
				bos = new BufferedOutputStream(fos);
				eos = new ASCII85OutputStream(bos);
				IOUtils.copyLarge(bis, eos);
				break;
			case BASE91:
				bos = new BufferedOutputStream(fos);
				b91cli.encodeWrap(bis, bos);
				break;
			case CRC16:
				CRC16OutputStream c16os = new CRC16OutputStream();
				IOUtils.copyLarge(bis, c16os);
				c16os.close();
				value = String.format("%04x", c16os.getValue());
				IOUtils.write(value + " *" + fileName, fos, charset);
				break;
			case CRC32:
				CRC32OutputStream c32os = new CRC32OutputStream();
				IOUtils.copyLarge(bis, c32os);
				c32os.close();
				value = String.format("%08x", c32os.getValue());
				IOUtils.write(fileName + ' ' + value, fos, charset); // sfv
				break;
			case MD2:
				value = DigestUtils.md2Hex(bis);
				IOUtils.write(value + " *" + fileName, fos, charset);
				break;
			case MD4:
				value = Hex.encodeHexString(DigestUtils.updateDigest(MessageDigest.getInstance(CodecAlgorithm.MD4.name(), MD4_PROVIDER), bis).digest());
				IOUtils.write(value + " *" + fileName, fos, charset);
				break;
			case MD5:
				value = DigestUtils.md5Hex(bis);
				IOUtils.write(value + " *" + fileName, fos, charset);
				break;
			case SHA1:
				value = DigestUtils.sha1Hex(bis);
				IOUtils.write(value + " *" + fileName, fos, charset);
				break;
			case SHA256:
				value = DigestUtils.sha256Hex(bis);
				IOUtils.write(value + " *" + fileName, fos, charset);
				break;
			case SHA384:
				value = DigestUtils.sha384Hex(bis);
				IOUtils.write(value + " *" + fileName, fos, charset);
				break;
			case SHA512:
				value = DigestUtils.sha512Hex(bis);
				IOUtils.write(value + " *" + fileName, fos, charset);
				break;
			default:
				throw new IllegalStateException();
			}
		}
		catch (final Exception e) {
			IOUtils.closeQuietly(eos);
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(fos);
			try {
				if (!outputFile.delete()) {
					outputFile.deleteOnExit();
				}
			}
			catch (final Exception de) {
				logger.log(Level.WARNING, Messages.get("err.cannot.delete.file", outputFile), de);
			}
			throw new IllegalStateException(Messages.get("err.cannot.encode", algorithm.getName()), e);
		}
		finally {
			IOUtils.closeQuietly(eos);
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(bis);
			IOUtils.closeQuietly(fis);
		}
		return value;
	}

	private String decode(final File inputFile, final File outputFile) {
		String value = null;
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		InputStream dis = null;
		try {
			fis = new FileInputStream(inputFile);
			bis = new BufferedInputStream(fis);
			fos = new FileOutputStream(outputFile);
			bos = new BufferedOutputStream(fos);
			switch (algorithm) {
			case BASE16:
				Base16.decode(bis, bos);
				break;
			case BASE32:
				dis = new Base32InputStream(bis);
				IOUtils.copyLarge(dis, bos);
				break;
			case BASE64:
				dis = new Base64InputStream(bis);
				IOUtils.copyLarge(dis, bos);
				break;
			case ASCII85:
				dis = new Ascii85InputStream(bis);
				IOUtils.copyLarge(dis, bos);
				break;
			case BASE91:
				b91cli.decode(bis, bos);
				break;
			default:
				if (!outputFile.delete()) {
					outputFile.deleteOnExit();
				}
				throw new IllegalStateException();
			}
		}
		catch (final Exception e) {
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(fos);
			try {
				if (!outputFile.delete()) {
					outputFile.deleteOnExit();
				}
			}
			catch (final Exception de) {
				logger.log(Level.WARNING, Messages.get("err.cannot.delete.file", outputFile), de);
			}
			throw new IllegalStateException(Messages.get("err.cannot.decode", algorithm.getName()), e);
		}
		finally {
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(fos);
			IOUtils.closeQuietly(dis);
			IOUtils.closeQuietly(bis);
			IOUtils.closeQuietly(fis);
		}
		return value;
	}

	private String encode(final String input) {
		String value = null;
		try {
			switch (algorithm) {
			case BASE16:
				value = Base16.encode(input.getBytes(charset));
				break;
			case BASE32:
				value = new Base32().encodeAsString(input.getBytes(charset));
				break;
			case BASE64:
				value = Base64.encodeBase64String(input.getBytes(charset));
				break;
			case ASCII85:
				value = Ascii85.encode(input.getBytes(charset));
				break;
			case BASE91:
				value = Base91.encode(input.getBytes(charset));
				break;
			case CRC16:
				final CRC16 crc16 = new CRC16();
				crc16.update(input.getBytes(charset));
				value = String.format("%04x", crc16.getValue());
				break;
			case CRC32:
				final CRC32 crc32 = new CRC32();
				crc32.update(input.getBytes(charset));
				value = String.format("%08x", crc32.getValue());
				break;
			case MD2:
				value = DigestUtils.md2Hex(input.getBytes(charset));
				break;
			case MD4:
				value = Hex.encodeHexString(MessageDigest.getInstance(CodecAlgorithm.MD4.name(), MD4_PROVIDER).digest(input.getBytes(charset)));
				break;
			case MD5:
				value = DigestUtils.md5Hex(input.getBytes(charset));
				break;
			case SHA1:
				value = DigestUtils.sha1Hex(input.getBytes(charset));
				break;
			case SHA256:
				value = DigestUtils.sha256Hex(input.getBytes(charset));
				break;
			case SHA384:
				value = DigestUtils.sha384Hex(input.getBytes(charset));
				break;
			case SHA512:
				value = DigestUtils.sha512Hex(input.getBytes(charset));
				break;
			default:
				throw new IllegalStateException();
			}
		}
		catch (final Exception e) {
			throw new IllegalStateException(Messages.get("err.cannot.encode", algorithm.getName()), e);
		}
		return value;
	}

	private String decode(final String input) {
		String value = null;
		try {
			switch (algorithm) {
			case BASE16:
				value = new String(Base16.decode(input), charset);
				break;
			case BASE32:
				value = new String(new Base32().decode(input), charset);
				break;
			case BASE64:
				value = new String(Base64.decodeBase64(input), charset);
				break;
			case ASCII85:
				value = new String(Ascii85.decode(input), charset);
				break;
			case BASE91:
				value = new String(Base91.decode(input), charset);
				break;
			default:
				throw new IllegalStateException();
			}
		}
		catch (final Exception e) {
			throw new IllegalStateException(Messages.get("err.cannot.decode", algorithm.getName()), e);
		}
		return value;
	}

	public CodecAlgorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(final CodecAlgorithm algorithm) {
		this.algorithm = algorithm;
	}

	public CodecMode getMode() {
		return mode;
	}

	public void setMode(final CodecMode mode) {
		this.mode = mode;
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(final Charset charset) {
		this.charset = charset;
	}

}
