package it.albertus.codec.gui.listener;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.gui.CodecGui;
import it.albertus.codec.gui.ProcessFileJob;

public class ProcessFileAction {

	protected final CodecGui gui;

	public ProcessFileAction(final CodecGui gui) {
		this.gui = gui;
	}

	/* Selezione file sorgente */
	protected String getSourceFile() {
		final FileDialog openDialog = new FileDialog(gui.getShell(), SWT.OPEN);
		if (CodecMode.DECODE.equals(gui.getEngine().getMode())) {
			openDialog.setFilterExtensions(new String[] { "*." + gui.getEngine().getAlgorithm().name().toLowerCase() + "; *." + gui.getEngine().getAlgorithm().name().toUpperCase(), "*.*" });
		}
		return openDialog.open();
	}

	/* Selezione file destinazione */
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

	protected void run(final String sourceFileName, final String destinationFileName) {
		if (sourceFileName != null && destinationFileName != null) {
			/* Disabilitazione controlli durante l'esecuzione */
			gui.disableControls();

			/* Impostazione puntatore del mouse "Occupato" */
			gui.getShell().setCursor(gui.getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));

			new ProcessFileJob(gui, new File(sourceFileName), new File(destinationFileName)).schedule();
		}
	}

}
