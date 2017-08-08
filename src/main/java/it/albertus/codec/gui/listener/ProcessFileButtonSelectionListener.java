package it.albertus.codec.gui.listener;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import it.albertus.codec.gui.CodecGui;

public class ProcessFileButtonSelectionListener extends ProcessFileAction implements SelectionListener {

	public ProcessFileButtonSelectionListener(final CodecGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		final String sourceFileName = getSourceFile();
		if (sourceFileName != null && sourceFileName.length() > 0) {
			final String destinationFileName = getDestinationFile(sourceFileName);
			if (destinationFileName != null && destinationFileName.length() > 0) {
				execute(sourceFileName, destinationFileName);
			}
		}
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent se) {/* Ignore */}

}
