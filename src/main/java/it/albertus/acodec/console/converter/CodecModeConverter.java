package it.albertus.acodec.console.converter;

import it.albertus.acodec.engine.CodecMode;
import lombok.NonNull;
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

		private static final long serialVersionUID = 7251461183775942243L;

		private InvalidModeException(@NonNull final String value) {
			super(value);
		}
	}

}
