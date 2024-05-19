package io.github.albertus82.acodec.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import io.github.albertus82.acodec.gui.CodecGui;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExitListener extends SelectionAdapter implements SelectionListener, Listener {

	@NonNull
	private final CodecGui gui;

	private void disposeAll() {
		final Shell shell = gui.getShell();
		if (shell != null && !shell.isDisposed()) {
			gui.saveSettings();
			shell.dispose();
		}
		final Display display = Display.getCurrent();
		if (display != null) {
			display.dispose(); // Fix close not working on Windows 10 when iconified
		}
	}

	// Shell close command & macOS menu
	@Override
	public void handleEvent(final Event event) {
		disposeAll();
	}

	/* Menu */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		disposeAll();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

}
