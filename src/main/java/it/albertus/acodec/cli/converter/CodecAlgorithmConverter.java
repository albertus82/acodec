package it.albertus.acodec.cli.converter;

import it.albertus.acodec.common.engine.CodecAlgorithm;
import lombok.NonNull;
import picocli.CommandLine.ITypeConverter;

public class CodecAlgorithmConverter implements ITypeConverter<CodecAlgorithm> {

	@Override
	public CodecAlgorithm convert(final String arg) throws InvalidAlgorithmException {
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getName().equalsIgnoreCase(arg) || ca.name().equalsIgnoreCase(arg) || ca.getAliases().stream().anyMatch(arg::equalsIgnoreCase)) {
				return ca;
			}
		}
		throw new InvalidAlgorithmException(arg);
	}

	public class InvalidAlgorithmException extends Exception {

		private static final long serialVersionUID = -996443425083357570L;

		private InvalidAlgorithmException(@NonNull final String value) {
			super(value);
		}
	}

}
