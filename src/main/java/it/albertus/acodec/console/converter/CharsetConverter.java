package it.albertus.acodec.console.converter;

import java.nio.charset.Charset;

import picocli.CommandLine.ITypeConverter;

public class CharsetConverter implements ITypeConverter<Charset> {

	@Override
	public Charset convert(final String arg) throws InvalidCharsetException {
		try {
			return Charset.forName(arg);
		}
		catch (final Exception e) {
			throw new InvalidCharsetException(arg, e);
		}
	}

	public class InvalidCharsetException extends Exception {

		private static final long serialVersionUID = -8053027081248048909L;

		private InvalidCharsetException(final String value, final Throwable cause) {
			super(value, cause);
		}
	}

}
