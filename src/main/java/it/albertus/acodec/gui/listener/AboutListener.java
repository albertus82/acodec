package it.albertus.acodec.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.acodec.gui.AboutDialog;

public class AboutListener implements SelectionListener, Listener {

	private final IShellProvider provider;

	public AboutListener(final IShellProvider provider) {
		this.provider = provider;
	}

	private void openAboutDialog() {
		new AboutDialog(provider.getShell()).open();
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		openAboutDialog();
	}

	@Override
	public void handleEvent(final Event event) {
		openAboutDialog();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

}
