package it.albertus.codec.gui;

import it.albertus.codec.resources.Resources;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

public class ProcessFileJob extends Job {

	private final CodecGui gui;
	private final File inputFile;
	private final File outputFile;

	public ProcessFileJob(final CodecGui gui, final File inputFile, final File outputFile) {
		super(ProcessFileJob.class.getName());
		this.gui = gui;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		monitor.beginTask(this.toString(), 1);

		String result = null;
		Throwable throwable = null;
		try {
			result = gui.getEngine().run(inputFile, outputFile);
		}
		catch (Exception e) {
			throwable = e;
		}

		/* Riattivazione GUI al termine dell'operazione */
		updateGui(result, throwable);

		monitor.done();
		return Status.OK_STATUS;
	}

	private void updateGui(final String result, final Throwable throwable) {
		gui.getShell().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				gui.enableControls();
				if (result != null) {
					gui.getInputText().setText(inputFile.getName());
					gui.getOutputText().setText(result);
					gui.setDirty(true);
				}
				gui.getShell().setCursor(null);
				final MessageBox messageBox;
				if (throwable != null) {
					messageBox = new MessageBox(gui.getShell(), SWT.ICON_ERROR);
					messageBox.setMessage(Resources.get("msg.file.process.ko.message", throwable.getMessage()));
				}
				else {
					messageBox = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
					messageBox.setMessage(Resources.get("msg.file.process.ok.message"));
				}
				messageBox.setText(Resources.get("msg.application.name"));
				messageBox.open();
			}
		});
	}

	@Override
	public String toString() {
		return "FileJob [inputFile=" + inputFile + ", outputFile=" + outputFile + "]";
	}

}
