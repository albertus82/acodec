package it.albertus.codec.gui;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.resources.Messages;

public class ProcessFileJob implements IRunnableWithProgress {

	private final CodecEngine engine;
	private final File inputFile;
	private final File outputFile;

	private String result;
	private Exception exception;

	public ProcessFileJob(final CodecEngine engine, final File inputFile, final File outputFile) {
		this.engine = engine;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	@Override
	public void run(final IProgressMonitor monitor) {
		monitor.beginTask(Messages.get("msg.file.process.task.name", inputFile.getName()), IProgressMonitor.UNKNOWN);

		try {
			result = engine.run(inputFile, outputFile);
		}
		catch (final Exception e) {
			exception = e;
		}

		monitor.done();
	}

	public String getResult() {
		return result;
	}

	public Exception getException() {
		return exception;
	}

}
