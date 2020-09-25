package it.albertus.acodec.gui;

public class ProcessFileException extends RuntimeException {

	private static final long serialVersionUID = -3091750202619436429L;

	public ProcessFileException(final Exception exception) {
		super(exception);
	}

}
