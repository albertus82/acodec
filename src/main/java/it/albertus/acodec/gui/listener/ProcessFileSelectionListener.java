package it.albertus.acodec.gui.listener;

import java.io.File;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import it.albertus.acodec.gui.CodecGui;

public class ProcessFileSelectionListener extends ProcessFileAction implements SelectionListener {

	public ProcessFileSelectionListener(final CodecGui gui) {
		super(gui);
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		getSourceFileName().ifPresent(sourceFileName -> {
			if (!sourceFileName.isEmpty()) {
				getDestinationFileName(sourceFileName).ifPresent(destinationFileName -> {
					if (!destinationFileName.isEmpty()) {
						execute(new File(sourceFileName), new File(destinationFileName));
					}
				});
			}
		});
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

}
