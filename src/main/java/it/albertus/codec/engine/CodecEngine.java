package it.albertus.codec.engine;

import it.albertus.codec.resources.Resources;

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

	public void run(final InputStream inputStream, final OutputStream outputStream) throws IOException {
		if (inputStream.available() == 0) {
			throw new IllegalStateException(Resources.get("msg.missing.input"));
		}
		if (algorithm != null) {
			switch (mode) {
			case DECODE:
				decode(inputStream, outputStream);
				return;
			case ENCODE:
				encode(inputStream, outputStream);
				return;
			default:
				throw new IllegalStateException("Invalid mode");
			}
		}
		else {
			throw new IllegalStateException(Resources.get("msg.missing.algorithm"));
		}
	}

	private void encode(final InputStream inputStream, OutputStream outputStream) throws IOException {
		switch (algorithm) {
		case BASE16:
			outputStream.write(Base16.encode(IOUtils.toByteArray(inputStream)).getBytes(charset));
			break;
		case BASE32:
			outputStream = new BaseNCodecOutputStream(outputStream, new Base32(79), true);
			IOUtils.copyLarge(inputStream, outputStream);
			break;
		case BASE64:
			outputStream = new Base64OutputStream(outputStream);
			IOUtils.copyLarge(inputStream, outputStream);
			break;
		case MD2:
			IOUtils.write(Hex.encodeHexString(DigestUtils.md2(inputStream)), outputStream, charset);
			break;
		case MD4:
			try {
				IOUtils.write(Hex.encodeHexString(DigestUtils.updateDigest(MessageDigest.getInstance(CodecAlgorithm.MD4.getName(), MD4_PROVIDER), inputStream).digest()), outputStream, charset);
			}
			catch (NoSuchAlgorithmException nsae) {
				throw new IllegalStateException(Resources.get("err.cannot.encode", algorithm.getName()), nsae);
			}
			break;
		case MD5:
			IOUtils.write(Hex.encodeHexString(DigestUtils.md5(inputStream)), outputStream, charset);
			break;
		case SHA1:
			IOUtils.write(Hex.encodeHexString(DigestUtils.sha1(inputStream)), outputStream, charset);
			break;
		case SHA256:
			IOUtils.write(Hex.encodeHexString(DigestUtils.sha256(inputStream)), outputStream, charset);
			break;
		case SHA384:
			IOUtils.write(Hex.encodeHexString(DigestUtils.sha384(inputStream)), outputStream, charset);
			break;
		case SHA512:
			IOUtils.write(Hex.encodeHexString(DigestUtils.sha512(inputStream)), outputStream, charset);
			break;
		default:
			throw new IllegalStateException(Resources.get("err.cannot.encode", algorithm.getName()));
		}
		outputStream.close();
		inputStream.close();
	}

	private void decode(InputStream inputStream, final OutputStream outputStream) throws IOException {
		switch (algorithm) {
		case BASE16:
			outputStream.write(Base16.decode(IOUtils.toString(inputStream, charset)));
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
			throw new IllegalStateException(Resources.get("err.cannot.decode", algorithm.getName()));
		}
		outputStream.close();
		inputStream.close();
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
			return Hex.encodeHexString(DigestUtils.getMd2Digest().digest(input.getBytes(charset)));
		case MD4:
			try {
				return Hex.encodeHexString(MessageDigest.getInstance(CodecAlgorithm.MD4.getName(), MD4_PROVIDER).digest(input.getBytes(charset)));
			}
			catch (NoSuchAlgorithmException nsae) {
				break;
			}
		case MD5:
			return Hex.encodeHexString(DigestUtils.getMd5Digest().digest(input.getBytes(charset)));
		case SHA1:
			return Hex.encodeHexString(DigestUtils.getSha1Digest().digest(input.getBytes(charset)));
		case SHA256:
			return Hex.encodeHexString(DigestUtils.getSha256Digest().digest(input.getBytes(charset)));
		case SHA384:
			return Hex.encodeHexString(DigestUtils.getSha384Digest().digest(input.getBytes(charset)));
		case SHA512:
			return Hex.encodeHexString(DigestUtils.getSha512Digest().digest(input.getBytes(charset)));
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
