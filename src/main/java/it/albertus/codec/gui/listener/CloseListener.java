package it.albertus.codec.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CloseListener implements ShellListener, SelectionListener, Listener {

	private final IShellProvider provider;

	public CloseListener(final IShellProvider provider) {
		this.provider = provider;
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		provider.getShell().dispose();
		event.display.dispose();
	}

	@Override
	public void handleEvent(final Event event) {
		provider.getShell().dispose();
		event.display.dispose();
	}

	@Override
	public void shellClosed(final ShellEvent event) {
		provider.getShell().dispose();
		event.display.dispose();
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
