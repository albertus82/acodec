package it.albertus.acodec.engine;

import java.util.zip.Adler32;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.codec.digest.PureJavaCrc32C;

import it.albertus.acodec.resources.Messages;
import it.albertus.util.CRC16;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringCodec {

	@NonNull
	private final CodecConfig config;

	public String run(final String input) throws EncoderException, DecoderException {
		if (config.getAlgorithm() == null) {
			throw new IllegalStateException(Messages.get("msg.missing.algorithm"));
		}
		if (input == null || input.isEmpty()) {
			throw new IllegalStateException(Messages.get("msg.missing.input"));
		}
		switch (config.getMode()) {
		case DECODE:
			return decode(input);
		case ENCODE:
			return encode(input);
		default:
			throw new UnsupportedOperationException(Messages.get("err.invalid.mode", config.getMode()));
		}
	}

	private String encode(final String input) throws EncoderException {
		try {
			final byte[] bytes = input.getBytes(config.getCharset());
			switch (config.getAlgorithm()) {
			case BASE16:
				return Base16.encode(bytes);
			case BASE32:
				return new Base32().encodeAsString(bytes);
			case BASE32HEX:
				return new Base32(true).encodeAsString(bytes);
			case BASE64:
				return Base64.encodeBase64String(bytes);
			case BASE64URL:
				return Base64.encodeBase64URLSafeString(bytes);
			case ASCII85:
				return Ascii85.encode(bytes);
			case BASE91:
				return Base91.encode(bytes);
			case CRC16:
				final CRC16 crc16 = new CRC16();
				crc16.update(bytes);
				return String.format("%04x", crc16.getValue());
			case CRC32:
				final PureJavaCrc32 crc32 = new PureJavaCrc32();
				crc32.update(bytes, 0, bytes.length);
				return String.format("%08x", crc32.getValue());
			case CRC32C:
				final PureJavaCrc32C crc32c = new PureJavaCrc32C();
				crc32c.update(bytes, 0, bytes.length);
				return String.format("%08x", crc32c.getValue());
			case ADLER32:
				final Adler32 adler32 = new Adler32();
				adler32.update(bytes);
				return String.format("%08x", adler32.getValue());
			default:
				return config.getAlgorithm().createDigestUtils().digestAsHex(bytes);
			}
		}
		catch (final Exception e) {
			throw new EncoderException(Messages.get("err.cannot.encode", config.getAlgorithm().getName()), e);
		}
	}

	private String decode(final String input) throws DecoderException {
		try {
			switch (config.getAlgorithm()) {
			case BASE16:
				return new String(Base16.decode(input), config.getCharset());
			case BASE32:
				return new String(new Base32().decode(input), config.getCharset());
			case BASE32HEX:
				return new String(new Base32(true).decode(input), config.getCharset());
			case BASE64:
			case BASE64URL:
				return new String(Base64.decodeBase64(input), config.getCharset());
			case ASCII85:
				return new String(Ascii85.decode(input), config.getCharset());
			case BASE91:
				return new String(Base91.decode(input), config.getCharset());
			default:
				throw new UnsupportedOperationException(Messages.get("err.invalid.algorithm", config.getAlgorithm().getName()));
			}
		}
		catch (final Exception e) {
			throw new DecoderException(Messages.get("err.cannot.decode", config.getAlgorithm().getName()), e);
		}
	}

}
