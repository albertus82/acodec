package it.albertus.acodec.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CloseListener implements ShellListener, SelectionListener, Listener {

	private final IShellProvider provider;

	public CloseListener(final IShellProvider provider) {
		this.provider = provider;
	}

	private void disposeShellAndDisplay() {
		provider.getShell().dispose();
		final Display display = Display.getCurrent();
		if (display != null) {
			display.dispose(); // Fix close not working on Windows 10 when iconified
		}
	}

	/* OS X Menu */
	@Override
	public void handleEvent(final Event event) {
		disposeShellAndDisplay();
	}

	/* Menu */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		disposeShellAndDisplay();
	}

	/* Shell close command */
	@Override
	public void shellClosed(final ShellEvent event) {
		disposeShellAndDisplay();
	}

	@Override
	public void shellActivated(final ShellEvent event) {/* Ignore */}

	@Override
	public void shellDeactivated(final ShellEvent event) {/* Ignore */}

	@Override
	public void shellDeiconified(final ShellEvent event) {/* Ignore */}

	@Override
	public void shellIconified(final ShellEvent event) {/* Ignore */}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

}
