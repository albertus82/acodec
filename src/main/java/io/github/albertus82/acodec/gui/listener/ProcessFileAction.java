package io.github.albertus82.acodec.gui.listener;

import static io.github.albertus82.acodec.common.engine.CodecMode.DECODE;
import static io.github.albertus82.acodec.common.engine.CodecMode.ENCODE;
import static io.github.albertus82.acodec.gui.GuiStatus.DIRTY;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Optional;
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

import io.github.albertus82.acodec.common.engine.Cancelable;
import io.github.albertus82.acodec.common.engine.CodecAlgorithm;
import io.github.albertus82.acodec.common.engine.CodecConfig;
import io.github.albertus82.acodec.common.engine.ProcessFileTask;
import io.github.albertus82.acodec.common.resources.Messages;
import io.github.albertus82.acodec.gui.CodecGui;
import io.github.albertus82.acodec.gui.Images;
import io.github.albertus82.acodec.gui.ProcessFileException;
import io.github.albertus82.acodec.gui.ProcessFileRunnable;
import io.github.albertus82.acodec.gui.resources.GuiMessages;
import io.github.albertus82.jface.DisplayThreadExecutor;
import io.github.albertus82.jface.DisplayThreadExecutor.Mode;
import io.github.albertus82.jface.EnhancedErrorDialog;
import io.github.albertus82.jface.SwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class ProcessFileAction {

	private static final Messages messages = GuiMessages.INSTANCE;

	@NonNull
	protected final CodecGui gui;

	public Optional<String> getSourceFileName() {
		final FileDialog openDialog = new FileDialog(gui.getShell(), SWT.OPEN);
		if (DECODE.equals(gui.getMode())) {
			openDialog.setFilterExtensions(buildFilterExtensions(gui.getAlgorithm()));
		}
		return Optional.ofNullable(openDialog.open());
	}

	public Optional<String> getDestinationFileName(@NonNull final String sourceFileName) {
		final FileDialog saveDialog = new FileDialog(gui.getShell(), SWT.SAVE);
		saveDialog.setOverwrite(true);
		final File sourceFile = new File(sourceFileName);
		saveDialog.setFilterPath(sourceFile.getParent());
		if (ENCODE.equals(gui.getMode())) {
			final CodecAlgorithm algorithm = gui.getAlgorithm();
			saveDialog.setFilterExtensions(buildFilterExtensions(algorithm));
			saveDialog.setFileName(sourceFile.getName() + '.' + algorithm.getFileExtension().toLowerCase(Locale.ROOT));
		}
		else if (sourceFile.getName().indexOf('.') != -1) {
			saveDialog.setFileName(sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf('.')));
		}
		return Optional.ofNullable(getDestinationFileName(saveDialog));
	}

	public Optional<String> getDestinationFileName() {
		final FileDialog saveDialog = new FileDialog(gui.getShell(), SWT.SAVE);
		saveDialog.setOverwrite(true);
		if (ENCODE.equals(gui.getMode())) {
			final CodecAlgorithm algorithm = gui.getAlgorithm();
			saveDialog.setFilterExtensions(buildFilterExtensions(algorithm));
		}
		return Optional.ofNullable(getDestinationFileName(saveDialog));
	}

	private String getDestinationFileName(@NonNull final FileDialog saveDialog) {
		final String name = saveDialog.open();
		if (name != null && name.indexOf('.') == -1 && ENCODE.equals(gui.getMode())) {
			return name + '.' + gui.getAlgorithm().getFileExtension();
		}
		else {
			return name;
		}
	}

	public static String[] buildFilterExtensions(@NonNull final CodecAlgorithm algorithm) {
		final String extension = algorithm.getFileExtension();
		return new String[] { "*." + extension.toLowerCase(Locale.ROOT) + ";*." + extension.toUpperCase(Locale.ROOT), "*.*" };
	}

	public void execute(@NonNull final String inputString, @NonNull final File outputFile) {
		execute(new ProcessFileTask(new CodecConfig(gui.getMode(), gui.getAlgorithm(), gui.getCharset()), inputString, outputFile));
	}

	public void execute(@NonNull final File inputFile, @NonNull final File outputFile) {
		execute(new ProcessFileTask(new CodecConfig(gui.getMode(), gui.getAlgorithm(), gui.getCharset()), inputFile, outputFile));
	}

	private void execute(@NonNull final ProcessFileTask task) {
		try {
			final ProcessFileRunnable runnable = new ProcessFileRunnable(task);
			final LocalizedProgressMonitorDialog dialog = new LocalizedProgressMonitorDialog(gui.getShell(), task);
			dialog.setOpenOnRun(false);
			dialog.run(true, true, runnable); // execute in a separate thread
			if (task.getInputFile() != null) {
				final MessageBox box = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
				box.setMessage(messages.get("gui.message.file.process.ok.message"));
				box.setText(CodecGui.getApplicationName());
				box.open();
				runnable.getResult().ifPresent(result -> {
					if (gui.getInputText().getCharCount() == 0) { // don't overwrite user text if present
						gui.setInputText(task.getInputFile().getName(), DIRTY);
						gui.setOutputText(result, DIRTY);
					}
				});
			}
		}
		catch (final InterruptedException e) { // NOSONAR Either re-interrupt this method or rethrow the "InterruptedException" that can be caught here. "InterruptedException" should not be ignored (java:S2142)
			log.log(Level.FINE, "Operation canceled by the user:", e);
			final MessageBox box = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
			box.setMessage(messages.get("gui.message.file.process.cancel.message"));
			box.setText(CodecGui.getApplicationName());
			box.open();
		}
		catch (final InvocationTargetException e) {
			log.log(Level.WARNING, "Error processing file:", e);
			final String message;
			final Throwable throwable = e.getCause() instanceof ProcessFileException ? e.getCause().getCause() : e;
			if (throwable instanceof EncoderException) {
				message = messages.get("gui.error.cannot.encode", gui.getAlgorithm().getName());
			}
			else if (throwable instanceof DecoderException) {
				message = messages.get("gui.error.cannot.decode", gui.getAlgorithm().getName());
			}
			else if (throwable instanceof FileNotFoundException) {
				message = messages.get("gui.error.missing.file", throwable.getMessage());
			}
			else {
				message = messages.get("gui.error.unexpected");
			}
			EnhancedErrorDialog.openError(gui.getShell(), CodecGui.getApplicationName(), message, IStatus.WARNING, throwable, Images.getAppIconArray());
		}
		catch (final Exception e) {
			log.log(Level.SEVERE, "An unexpected error has occurred:", e);
			EnhancedErrorDialog.openError(gui.getShell(), CodecGui.getApplicationName(), messages.get("gui.error.unexpected"), IStatus.ERROR, e, Images.getAppIconArray());
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
