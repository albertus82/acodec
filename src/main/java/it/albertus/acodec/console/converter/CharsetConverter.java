package it.albertus.acodec.console.converter;

import java.nio.charset.Charset;

import it.albertus.acodec.resources.Messages;
import picocli.CommandLine.ITypeConverter;

public class CharsetConverter implements ITypeConverter<Charset> {

	@Override
	public Charset convert(final String arg) {
		try {
			return Charset.forName(arg);
		}
		catch (final Exception e) {
			throw new IllegalArgumentException(Messages.get("err.invalid.charset", arg) + System.lineSeparator(), e);
		}
	}

}
