package it.albertus.acodec.console.converter;

import it.albertus.acodec.engine.CodecMode;
import it.albertus.acodec.resources.Messages;
import picocli.CommandLine.ITypeConverter;

public class CodecModeConverter implements ITypeConverter<CodecMode> {

	@Override
	public CodecMode convert(final String arg) {
		for (final CodecMode cm : CodecMode.values()) {
			if (Character.toString(cm.getAbbreviation()).equalsIgnoreCase(arg)) {
				return cm;
			}
		}
		throw new IllegalArgumentException(Messages.get("err.invalid.mode", arg) + System.lineSeparator());
	}

}
