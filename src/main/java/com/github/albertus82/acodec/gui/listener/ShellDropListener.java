package com.github.albertus82.acodec.gui.listener;

import java.io.File;

import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;

import com.github.albertus82.acodec.gui.CodecGui;

import lombok.NonNull;

public class ShellDropListener extends ProcessFileAction implements DropTargetListener {

	public ShellDropListener(@NonNull final CodecGui gui) {
		super(gui);
	}

	@Override
	public void drop(@NonNull final DropTargetEvent event) {
		if (gui.getAlgorithm() != null) {
			if (event.data instanceof String[]) { // file
				final String[] data = (String[]) event.data;
				if (data.length == 1) {
					processFile(new File(data[0]));
				}
			}
			else if (event.data instanceof String) { // text
				processText(event.data.toString());
			}
		}
	}

	private void processText(@NonNull final String text) {
		gui.getInputText().setText(text);
	}

	private void processFile(@NonNull final File sourceFile) {
		if (sourceFile.exists() && !sourceFile.isDirectory()) {
			final String sourceFileName = sourceFile.getPath();
			getDestinationFileName(sourceFileName).ifPresent(destinationFileName -> {
				if (!destinationFileName.isEmpty()) {
					execute(sourceFile, new File(destinationFileName));
				}
			});
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
