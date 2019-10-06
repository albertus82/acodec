package it.albertus.codec.gui.listener;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.gui.CodecGui;
import it.albertus.codec.gui.Images;
import it.albertus.codec.gui.ProcessFileRunnable;
import it.albertus.codec.resources.Messages;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.util.logging.LoggerFactory;

public class ProcessFileAction {

	private static final String MSG_APPLICATION_NAME = "msg.application.name";

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
		final File sourceFile = new File(sourceFileName);
		saveDialog.setFilterPath(sourceFile.getParent());
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
			saveDialog.setFileName(sourceFile.getName() + '.' + extension.toLowerCase());
		}
		else {
			if (sourceFile.getName().indexOf('.') != -1) {
				saveDialog.setFileName(sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf('.')));
			}
		}
		return saveDialog.open();
	}

	protected void execute(final String sourceFileName, final String destinationFileName) {
		if (sourceFileName == null || destinationFileName == null) {
			throw new NullPointerException("File names cannot be null.");
		}
		try {
			final File inputFile = new File(sourceFileName);
			final File outputFile = new File(destinationFileName);
			final ProcessFileRunnable runnable = new ProcessFileRunnable(gui.getEngine(), inputFile, outputFile);
			new LocalizedProgressMonitorDialog(gui.getShell()).run(runnable); // execute in separate thread
			if (runnable.getResult() != null) { // result can be null in certain cases
				gui.getInputText().setText(inputFile.getName());
				gui.getOutputText().setText(runnable.getResult());
				gui.setDirty(true);
			}
			final MessageBox box = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
			box.setMessage(Messages.get("msg.file.process.ok.message"));
			box.setText(Messages.get(MSG_APPLICATION_NAME));
			box.open();
		}
		catch (final InvocationTargetException e) {
			logger.log(Level.WARNING, e.toString(), e);
			final String message;
			final Throwable throwable;
			if (e.getCause() != null) {
				message = e.getCause().getMessage() != null ? e.getCause().getMessage() : e.getCause().toString();
				throwable = e.getCause();
			}
			else {
				message = e.getMessage() != null ? e.getMessage() : e.toString();
				throwable = e;
			}
			EnhancedErrorDialog.openError(gui.getShell(), Messages.get(MSG_APPLICATION_NAME), message, IStatus.WARNING, throwable, Images.getMainIcons());
		}
		catch (final Exception e) {
			logger.log(Level.SEVERE, e.toString(), e);
			EnhancedErrorDialog.openError(gui.getShell(), Messages.get(MSG_APPLICATION_NAME), e.toString(), IStatus.ERROR, e, Images.getMainIcons());
		}
	}

	private static class LocalizedProgressMonitorDialog extends ProgressMonitorDialog {

		private LocalizedProgressMonitorDialog(final Shell parent) {
			super(parent);
		}

		@Override // improved localization
		public void create() {
			super.create();
			getShell().setText(Messages.get("lbl.process.file.dialog.title"));
			final Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
			if (cancelButton != null && !cancelButton.isDisposed()) {
				cancelButton.setText(Messages.get("lbl.process.file.dialog.button.cancel"));
			}
		}

		private void run(final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			super.run(true, false, runnable);
		}
	}

}
