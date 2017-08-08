package it.albertus.codec.gui.listener;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.gui.CodecGui;
import it.albertus.codec.gui.Images;
import it.albertus.codec.gui.ProcessFileJob;
import it.albertus.codec.resources.Messages;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.util.logging.LoggerFactory;

public class ProcessFileAction {

	private static final Logger logger = LoggerFactory.getLogger(ProcessFileAction.class);

	protected final CodecGui gui;

	public ProcessFileAction(final CodecGui gui) {
		this.gui = gui;
	}

	protected String getSourceFile() {
		final FileDialog openDialog = new FileDialog(gui.getShell(), SWT.OPEN);
		if (CodecMode.DECODE.equals(gui.getEngine().getMode())) {
			openDialog.setFilterExtensions(new String[] { "*." + gui.getEngine().getAlgorithm().name().toLowerCase() + "; *." + gui.getEngine().getAlgorithm().name().toUpperCase(), "*.*" });
		}
		return openDialog.open();
	}

	protected String getDestinationFile(final String sourceFileName) {
		final FileDialog saveDialog = new FileDialog(gui.getShell(), SWT.SAVE);
		saveDialog.setOverwrite(true);
		if (CodecMode.ENCODE.equals(gui.getEngine().getMode())) {
			final String extension;
			final CodecAlgorithm algorithm = gui.getEngine().getAlgorithm();
			if (CodecAlgorithm.CRC32.equals(algorithm)) {
				extension = "sfv";
			}
			else {
				extension = algorithm.name();
			}
			saveDialog.setFilterExtensions(new String[] { "*." + extension.toLowerCase() + ";*." + extension.toUpperCase(), "*.*" });
			saveDialog.setFileName(sourceFileName + '.' + extension.toLowerCase());
		}
		else {
			if (sourceFileName.indexOf('.') != -1) {
				saveDialog.setFileName(sourceFileName.substring(0, sourceFileName.lastIndexOf('.')));
			}
		}
		return saveDialog.open();
	}

	protected void execute(final String sourceFileName, final String destinationFileName) {
		if (sourceFileName != null && destinationFileName != null) {
			ProcessFileJob job = null;
			final File inputFile = new File(sourceFileName);
			final File outputFile = new File(destinationFileName);
			try {
				job = new ProcessFileJob(gui.getEngine(), inputFile, outputFile);
				new ProgressMonitorDialog(gui.getShell()) {
					@Override
					public void create() {
						super.create();
						getShell().setText(Messages.get("lbl.process.file.dialog.title"));
						final Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
						if (cancelButton != null && !cancelButton.isDisposed()) {
							cancelButton.setText(Messages.get("lbl.process.file.dialog.button.cancel"));
						}
					}
				}.run(true, false, job);
			}
			catch (final Exception e) {
				throw new IllegalStateException(e);
			}

			if (job.getResult() != null) {
				gui.getInputText().setText(inputFile.getName());
				gui.getOutputText().setText(job.getResult());
				gui.setDirty(true);
			}
			if (job.getException() != null) {
				logger.log(Level.SEVERE, job.getException().toString(), job.getException());
				EnhancedErrorDialog.openError(gui.getShell(), Messages.get("msg.application.name"), job.getException().toString(), IStatus.ERROR, job.getException(), Images.getMainIcons());
			}
			else {
				final MessageBox box = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
				box.setMessage(Messages.get("msg.file.process.ok.message"));
				box.setText(Messages.get("msg.application.name"));
				box.open();
			}
		}
	}

}
