package it.albertus.acodec.console.converter;

import it.albertus.acodec.engine.CodecAlgorithm;
import it.albertus.acodec.resources.Messages;
import picocli.CommandLine.ITypeConverter;

public class CodecAlgorithmConverter implements ITypeConverter<CodecAlgorithm> {

	@Override
	public CodecAlgorithm convert(final String arg) throws ConverterException {
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getName().equalsIgnoreCase(arg) || ca.name().equalsIgnoreCase(arg) || ca.getAliases().stream().anyMatch(arg::equalsIgnoreCase)) {
				return ca;
			}
		}
		throw new ConverterException(Messages.get("err.invalid.algorithm", arg));
	}

}
