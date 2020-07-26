package it.albertus.codec.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.codec.gui.AboutDialog;

public class AboutListener implements SelectionListener, Listener {

	private final IShellProvider provider;

	public AboutListener(final IShellProvider provider) {
		this.provider = provider;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		execute();
	}

	@Override
	public void handleEvent(final Event event) {
		execute();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

	private void execute() {
		new AboutDialog(provider.getShell()).open();
	}

}
