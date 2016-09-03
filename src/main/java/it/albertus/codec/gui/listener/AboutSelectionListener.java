package it.albertus.codec.gui.listener;

import it.albertus.codec.gui.AboutDialog;
import it.albertus.codec.resources.Messages;
import it.albertus.util.Version;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class AboutSelectionListener extends SelectionAdapter {

	private final IShellProvider gui;

	public AboutSelectionListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final AboutDialog aboutDialog = new AboutDialog(gui.getShell());
		aboutDialog.setText(Messages.get("lbl.about.title"));
		aboutDialog.setMessage(Messages.get("msg.application.name") + ' ' + Messages.get("msg.version", Version.getInstance().getNumber(), Version.getInstance().getDate()));
		aboutDialog.setApplicationUrl(Messages.get("msg.website"));
		aboutDialog.setIconUrl(Messages.get("msg.info.icon.site"));
		aboutDialog.open();
	}

}
