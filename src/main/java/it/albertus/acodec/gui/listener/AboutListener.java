package it.albertus.acodec.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.acodec.gui.AboutDialog;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AboutListener implements SelectionListener, Listener {

	@NonNull private final IShellProvider provider;

	private void openAboutDialog() {
		new AboutDialog(provider.getShell()).open();
	}

	@Override
	public void widgetSelected(final SelectionEvent event) {
		openAboutDialog();
	}

	@Override
	public void handleEvent(final Event event) {
		openAboutDialog();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

}
