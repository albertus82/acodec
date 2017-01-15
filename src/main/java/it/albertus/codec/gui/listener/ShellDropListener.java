package it.albertus.codec.gui.listener;

import java.io.File;

import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;

import it.albertus.codec.gui.CodecGui;

public class ShellDropListener extends ProcessFileAction implements DropTargetListener {

	public ShellDropListener(final CodecGui gui) {
		super(gui);
	}

	@Override
	public void drop(final DropTargetEvent event) {
		if (gui.getEngine().getAlgorithm() != null && event.data instanceof String[]) {
			final String[] data = (String[]) event.data;
			if (data.length == 1) {
				final File file = new File(data[0]);
				if (file.exists() && !file.isDirectory()) {
					final String sourceFileName = data[0];
					final String destinationFileName = getDestinationFile(sourceFileName);
					if (destinationFileName != null && destinationFileName.length() > 0) {
						run(sourceFileName, destinationFileName);
					}
				}
			}
		}
	}

	@Override
	public void dragEnter(final DropTargetEvent event) {/* Ignore */}

	@Override
	public void dragLeave(final DropTargetEvent event) {/* Ignore */}

	@Override
	public void dragOperationChanged(final DropTargetEvent event) {/* Ignore */}

	@Override
	public void dragOver(final DropTargetEvent event) {/* Ignore */}

	@Override
	public void dropAccept(final DropTargetEvent event) {/* Ignore */}

}
