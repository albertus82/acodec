package it.albertus.codec.engine;

import it.albertus.codec.resources.Resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

	public String run(final File inputFile, final File outputFile) throws IOException {
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

	private String encode(final File inputFile, final File outputFile) throws IOException {
		final String fileName;
		if (inputFile.getParentFile().getAbsolutePath().equals(outputFile.getParentFile().getAbsolutePath())) {
			fileName = inputFile.getName();
		}
		else {
			fileName = inputFile.getAbsolutePath();
		}
		InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
		OutputStream outputStream;
		String value = null;
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
			try {
				value = Hex.encodeHexString(DigestUtils.updateDigest(MessageDigest.getInstance(CodecAlgorithm.MD4.getName(), MD4_PROVIDER), inputStream).digest());
				outputStream = new FileOutputStream(outputFile);
				IOUtils.write(value + " *" + fileName, outputStream, charset);
			}
			catch (NoSuchAlgorithmException nsae) {
				throw new IllegalStateException(Resources.get("err.cannot.encode", algorithm.getName()), nsae);
			}
			finally {
				inputStream.close();
				outputFile.delete();
			}
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
			inputStream.close();
			outputFile.delete();
			throw new IllegalStateException(Resources.get("err.cannot.encode", algorithm.getName()));
		}
		outputStream.close();
		inputStream.close();
		return value;
	}

	private String decode(final File inputFile, final File outputFile) throws IOException {
		InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
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
			outputStream.close();
			inputStream.close();
			outputFile.delete();
			throw new IllegalStateException(Resources.get("err.cannot.decode", algorithm.getName()));
		}
		outputStream.close();
		inputStream.close();
		return null;
	}

	private String encode(final String input) {
		switch (algorithm) {
		case BASE16:
			return Base16.encode(input.getBytes(charset));
		case BASE32:
			return new Base32().encodeAsString(input.getBytes(charset));
		case BASE64:
			return Base64.encodeBase64String(input.getBytes(charset));
		case MD2:
			return DigestUtils.md2Hex(input.getBytes(charset));
		case MD4:
			try {
				return Hex.encodeHexString(MessageDigest.getInstance(CodecAlgorithm.MD4.getName(), MD4_PROVIDER).digest(input.getBytes(charset)));
			}
			catch (NoSuchAlgorithmException nsae) {
				break;
			}
		case MD5:
			return DigestUtils.md5Hex(input.getBytes(charset));
		case SHA1:
			return DigestUtils.sha1Hex(input.getBytes(charset));
		case SHA256:
			return DigestUtils.sha256Hex(input.getBytes(charset));
		case SHA384:
			return DigestUtils.sha384Hex(input.getBytes(charset));
		case SHA512:
			return DigestUtils.sha512Hex(input.getBytes(charset));
		default:
			break;
		}
		throw new IllegalStateException(Resources.get("err.cannot.encode", algorithm.getName()));
	}

	private String decode(final String input) {
		switch (algorithm) {
		case BASE16:
			return new String(Base16.decode(input), charset);
		case BASE32:
			return new String(new Base32().decode(input), charset);
		case BASE64:
			return new String(Base64.decodeBase64(input), charset);
		default:
			break;
		}
		throw new IllegalStateException(Resources.get("err.cannot.decode", algorithm.getName()));
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
