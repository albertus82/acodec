package it.albertus.acodec.console.converter;

import lombok.NonNull;

public class ConverterException extends Exception {

	private static final long serialVersionUID = 6526616825091993549L;

	public ConverterException(@NonNull final String message, @NonNull final Throwable cause) {
		super(message, cause);
	}

	public ConverterException(@NonNull final String message) {
		super(message);
	}

}
