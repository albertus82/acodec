package it.albertus.codec.gui;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public class FileJob extends Job {

	private final CodecGui gui;
	private final File inputFile;
	private final File outputFile;

	public FileJob(final CodecGui gui, final File inputFile, final File outputFile) {
		super(FileJob.class.getName());
		this.gui = gui;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		monitor.beginTask(this.toString(), 1);

		try {
			final String value = gui.getEngine().run(inputFile, outputFile);

			/* Riattivazione GUI al termine dell'operazione */
			gui.getShell().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					gui.enableControls();
					if (value != null) {
						gui.getInputText().setText(inputFile.getName());
						gui.getOutputText().setText(value);
						gui.setDirty(true);
					}
					gui.getShell().setCursor(null);
				}
			});
		}
		catch (Exception e) {
			e.printStackTrace(); // TODO Gestione errori!
		}

		monitor.done();
		return Status.OK_STATUS;
	}

	@Override
	public String toString() {
		return "FileJob [inputFile=" + inputFile + ", outputFile=" + outputFile + "]";
	}

}
