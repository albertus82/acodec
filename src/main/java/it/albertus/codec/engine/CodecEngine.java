package it.albertus.codec.engine;

import it.albertus.codec.resources.Resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;

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

public class CodecEngine {

	private static final MD4Provider MD4_PROVIDER = new MD4Provider();

	private CodecAlgorithm algorithm;
	private CodecMode mode = CodecMode.ENCODE;
	private Charset charset = Charset.defaultCharset();

	public String run(final String input) {
		if (input.length() == 0) {
			throw new IllegalStateException(Resources.get("msg.missing.input"));
		}
		if (algorithm != null) {
			switch (mode) {
			case DECODE:
				return decode(input);
			case ENCODE:
				return encode(input);
			default:
				throw new IllegalStateException("Invalid mode");
			}
		}
		else {
			throw new IllegalStateException(Resources.get("msg.missing.algorithm"));
		}
	}

	public String run(final File inputFile, final File outputFile) {
		if (!inputFile.exists()) {
			throw new IllegalStateException(Resources.get("msg.missing.input"));
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
			throw new IllegalStateException(Resources.get("msg.missing.algorithm"));
		}
	}

	private String encode(final File inputFile, final File outputFile) {
		String value = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		final String fileName;
		try {
			if (inputFile.getParentFile().getAbsolutePath().equals(outputFile.getParentFile().getAbsolutePath())) {
				fileName = inputFile.getName();
			}
			else {
				fileName = inputFile.getAbsolutePath();
			}
			inputStream = new BufferedInputStream(new FileInputStream(inputFile));
			switch (algorithm) {
			case BASE16:
				outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
				Base16.encode(inputStream, outputStream);
				break;
			case BASE32:
				outputStream = new BaseNCodecOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)), new Base32(79), true);
				IOUtils.copyLarge(inputStream, outputStream);
				break;
			case BASE64:
				outputStream = new Base64OutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
				IOUtils.copyLarge(inputStream, outputStream);
				break;
			case MD2:
				value = DigestUtils.md2Hex(inputStream);
				outputStream = new FileOutputStream(outputFile);
				IOUtils.write(value + " *" + fileName, outputStream, charset);
				break;
			case MD4:
				value = Hex.encodeHexString(DigestUtils.updateDigest(MessageDigest.getInstance(CodecAlgorithm.MD4.getName(), MD4_PROVIDER), inputStream).digest());
				outputStream = new FileOutputStream(outputFile);
				IOUtils.write(value + " *" + fileName, outputStream, charset);
				break;
			case MD5:
				value = DigestUtils.md5Hex(inputStream);
				outputStream = new FileOutputStream(outputFile);
				IOUtils.write(value + " *" + fileName, outputStream, charset);
				break;
			case SHA1:
				value = DigestUtils.sha1Hex(inputStream);
				outputStream = new FileOutputStream(outputFile);
				IOUtils.write(value + " *" + fileName, outputStream, charset);
				break;
			case SHA256:
				value = DigestUtils.sha256Hex(inputStream);
				outputStream = new FileOutputStream(outputFile);
				IOUtils.write(value + " *" + fileName, outputStream, charset);
				break;
			case SHA384:
				value = DigestUtils.sha384Hex(inputStream);
				outputStream = new FileOutputStream(outputFile);
				IOUtils.write(value + " *" + fileName, outputStream, charset);
				break;
			case SHA512:
				value = DigestUtils.sha512Hex(inputStream);
				outputStream = new FileOutputStream(outputFile);
				IOUtils.write(value + " *" + fileName, outputStream, charset);
				break;
			default:
				throw new IllegalStateException();
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(Resources.get("err.cannot.encode", algorithm.getName()), e);
		}
		finally {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(inputStream);
		}
		return value;
	}

	private String decode(final File inputFile, final File outputFile) {
		String value = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(inputFile));
			outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
			switch (algorithm) {
			case BASE16:
				Base16.decode(inputStream, outputStream);
				break;
			case BASE32:
				inputStream = new Base32InputStream(inputStream);
				IOUtils.copyLarge(inputStream, outputStream);
				break;
			case BASE64:
				inputStream = new Base64InputStream(inputStream);
				IOUtils.copyLarge(inputStream, outputStream);
				break;
			default:
				outputFile.delete();
				throw new IllegalStateException();
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(Resources.get("err.cannot.decode", algorithm.getName()), e);
		}
		finally {
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(inputStream);
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
			case MD2:
				value = DigestUtils.md2Hex(input.getBytes(charset));
				break;
			case MD4:
				value = Hex.encodeHexString(MessageDigest.getInstance(CodecAlgorithm.MD4.getName(), MD4_PROVIDER).digest(input.getBytes(charset)));
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
		catch (Exception e) {
			throw new IllegalStateException(Resources.get("err.cannot.encode", algorithm.getName()), e);
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
			default:
				throw new IllegalStateException();
			}
		}
		catch (Exception e) {
			throw new IllegalStateException(Resources.get("err.cannot.decode", algorithm.getName()), e);
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
