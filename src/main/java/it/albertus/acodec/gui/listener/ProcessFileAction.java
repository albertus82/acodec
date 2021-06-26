package it.albertus.acodec.gui.listener;

import static it.albertus.acodec.common.engine.CodecMode.DECODE;
import static it.albertus.acodec.common.engine.CodecMode.ENCODE;
import static it.albertus.acodec.gui.GuiStatus.DIRTY;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import it.albertus.acodec.common.engine.Cancelable;
import it.albertus.acodec.common.engine.CodecAlgorithm;
import it.albertus.acodec.common.engine.CodecConfig;
import it.albertus.acodec.common.engine.ProcessFileTask;
import it.albertus.acodec.common.resources.Messages;
import it.albertus.acodec.gui.CodecGui;
import it.albertus.acodec.gui.Images;
import it.albertus.acodec.gui.ProcessFileException;
import it.albertus.acodec.gui.ProcessFileRunnable;
import it.albertus.acodec.gui.resources.GuiMessages;
import it.albertus.jface.DisplayThreadExecutor;
import it.albertus.jface.DisplayThreadExecutor.Mode;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.SwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class ProcessFileAction {

	private static final String GUI_MESSAGE_APPLICATION_NAME = "gui.message.application.name";

	private static final Messages messages = GuiMessages.INSTANCE;

	@NonNull protected final CodecGui gui;

	protected String getSourceFile() {
		final FileDialog openDialog = new FileDialog(gui.getShell(), SWT.OPEN);
		if (DECODE.equals(gui.getMode())) {
			openDialog.setFilterExtensions(buildFilterExtensions(gui.getAlgorithm()));
		}
		return openDialog.open();
	}

	protected String getDestinationFile(@NonNull final String sourceFileName) {
		final FileDialog saveDialog = new FileDialog(gui.getShell(), SWT.SAVE);
		saveDialog.setOverwrite(true);
		final File sourceFile = new File(sourceFileName);
		saveDialog.setFilterPath(sourceFile.getParent());
		if (ENCODE.equals(gui.getMode())) {
			final CodecAlgorithm algorithm = gui.getAlgorithm();
			saveDialog.setFilterExtensions(buildFilterExtensions(algorithm));
			saveDialog.setFileName(sourceFile.getName() + '.' + algorithm.getFileExtension().toLowerCase(Locale.ROOT));
		}
		else {
			if (sourceFile.getName().indexOf('.') != -1) {
				saveDialog.setFileName(sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf('.')));
			}
		}
		return saveDialog.open();
	}

	static String[] buildFilterExtensions(@NonNull final CodecAlgorithm algorithm) {
		final String extension = algorithm.getFileExtension();
		return new String[] { "*." + extension.toLowerCase(Locale.ROOT) + ";*." + extension.toUpperCase(Locale.ROOT), "*.*" };
	}

	protected void execute(@NonNull final String sourceFileName, @NonNull final String destinationFileName) {
		try {
			final File inputFile = new File(sourceFileName);
			final File outputFile = new File(destinationFileName);
			final ProcessFileTask task = new ProcessFileTask(new CodecConfig(gui.getMode(), gui.getAlgorithm(), gui.getCharset()), inputFile, outputFile);
			final ProcessFileRunnable runnable = new ProcessFileRunnable(task);
			final LocalizedProgressMonitorDialog dialog = new LocalizedProgressMonitorDialog(gui.getShell(), task);
			dialog.setOpenOnRun(false);
			dialog.run(true, true, runnable); // execute in separate thread
			final MessageBox box = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
			box.setMessage(messages.get("gui.message.file.process.ok.message"));
			box.setText(messages.get(GUI_MESSAGE_APPLICATION_NAME));
			box.open();
			runnable.getResult().ifPresent(result -> {
				gui.setInputText(inputFile.getName(), DIRTY);
				gui.setOutputText(result, DIRTY);
			});
		}
		catch (final InterruptedException e) { // NOSONAR
			final MessageBox box = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
			box.setMessage(messages.get("gui.message.file.process.cancel.message"));
			box.setText(messages.get(GUI_MESSAGE_APPLICATION_NAME));
			box.open();
		}
		catch (final InvocationTargetException e) {
			log.log(Level.WARNING, e.toString(), e);
			final String message;
			final Throwable throwable = e.getCause() instanceof ProcessFileException ? e.getCause().getCause() : e;
			if (throwable instanceof EncoderException) {
				message = messages.get("gui.error.cannot.encode", gui.getAlgorithm().getName());
			}
			else if (throwable instanceof DecoderException) {
				message = messages.get("gui.error.cannot.decode", gui.getAlgorithm().getName());
			}
			else if (throwable instanceof FileNotFoundException) {
				message = messages.get("gui.message.missing.file", throwable.getMessage());
			}
			else {
				message = messages.get("gui.error.unexpected.error");
			}
			EnhancedErrorDialog.openError(gui.getShell(), messages.get(GUI_MESSAGE_APPLICATION_NAME), message, IStatus.WARNING, throwable, Images.getAppIconArray());
		}
		catch (final Exception e) {
			log.log(Level.SEVERE, e.toString(), e);
			EnhancedErrorDialog.openError(gui.getShell(), messages.get(GUI_MESSAGE_APPLICATION_NAME), e.toString(), IStatus.ERROR, e, Images.getAppIconArray());
		}
	}

	private static class LocalizedProgressMonitorDialog extends ProgressMonitorDialog {

		private static final int OPEN_DELAY_MILLIS = 1000;

		private final Cancelable cancelable;

		private LocalizedProgressMonitorDialog(final Shell shell, final Cancelable cancelable) {
			super(shell);
			this.cancelable = cancelable;
		}

		@Override // improved localization
		public void create() {
			super.create();
			getShell().setText(messages.get("gui.label.process.file.dialog.title"));
			final Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
			if (cancelButton != null && !cancelButton.isDisposed()) {
				cancelButton.setText(messages.get("gui.label.process.file.dialog.button.cancel"));
			}
		}

		@Override
		protected void cancelPressed() {
			super.cancelPressed();
			progressIndicator.showError();
			cancelable.cancel();
		}

		@Override
		protected void aboutToRun() {
			super.aboutToRun();
			SwtUtils.blockShell(getParentShell());
			final Thread opener = new Thread(() -> {
				try {
					TimeUnit.MILLISECONDS.sleep(OPEN_DELAY_MILLIS); // do not show the progress dialog if processing takes a short time
					new DisplayThreadExecutor(getParentShell(), Mode.ASYNC).execute(() -> {
						if (getShell() != null && !getShell().isDisposed()) {
							open();
						}
					});
				}
				catch (final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				finally {
					new DisplayThreadExecutor(getParentShell(), Mode.ASYNC).execute(() -> SwtUtils.unblockShell(getParentShell()));
				}
			});
			opener.setDaemon(false);
			opener.start();
		}
	}

}
