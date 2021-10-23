package io.github.albertus82.acodec.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExitListener extends ShellAdapter implements SelectionListener, Listener {

	@NonNull
	private final IShellProvider shellProvider;

	private void disposeAll() {
		shellProvider.getShell().dispose();
		final Display display = Display.getCurrent();
		if (display != null) {
			display.dispose(); // Fix close not working on Windows 10 when iconified
		}
	}

	/* macOS Menu */
	@Override
	public void handleEvent(final Event event) {
		disposeAll();
	}

	/* Menu */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		disposeAll();
	}

	/* Shell close command */
	@Override
	public void shellClosed(final ShellEvent event) {
		disposeAll();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

}
