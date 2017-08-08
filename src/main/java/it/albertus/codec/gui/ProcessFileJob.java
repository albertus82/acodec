package it.albertus.codec.gui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.codec.resources.Messages;

public class ProcessFileJob implements IRunnableWithProgress {

	private final CodecGui gui;
	private final File inputFile;
	private final File outputFile;

	public ProcessFileJob(final CodecGui gui, final File inputFile, final File outputFile) {
		this.gui = gui;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

	@Override
	public void run(final IProgressMonitor monitor) throws InvocationTargetException {
		monitor.beginTask(this.toString(), IProgressMonitor.UNKNOWN);

		String result = null;
		Throwable throwable = null;
		try {
			result = gui.getEngine().run(inputFile, outputFile);
		}
		catch (Exception e) {
			throw new InvocationTargetException(e);
		}

		/* Riattivazione GUI al termine dell'operazione */
		updateGui(result, throwable);

		monitor.done();
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
					messageBox.setMessage(Messages.get("msg.file.process.ko.message", throwable.getMessage()));
				}
				else {
					messageBox = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
					messageBox.setMessage(Messages.get("msg.file.process.ok.message"));
				}
				messageBox.setText(Messages.get("msg.application.name"));
				messageBox.open();
			}
		});
	}

}
