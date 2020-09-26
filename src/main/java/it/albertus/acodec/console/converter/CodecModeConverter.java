package it.albertus.acodec.console.converter;

import it.albertus.acodec.engine.CodecMode;
import picocli.CommandLine.ITypeConverter;

public class CodecModeConverter implements ITypeConverter<CodecMode> {

	@Override
	public CodecMode convert(final String arg) throws InvalidModeException {
		for (final CodecMode cm : CodecMode.values()) {
			if (Character.toString(cm.getAbbreviation()).equalsIgnoreCase(arg)) {
				return cm;
			}
		}
		throw new InvalidModeException(arg);
	}

	public class InvalidModeException extends Exception {

		private static final long serialVersionUID = -996443425083357570L;

		private InvalidModeException(final String value) {
			super(value);
		}
	}

}
