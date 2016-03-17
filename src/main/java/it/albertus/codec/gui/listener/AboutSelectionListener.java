package it.albertus.codec.gui.listener;

import it.albertus.codec.resources.Resources;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;

public class AboutSelectionListener extends SelectionAdapter {

	private final IShellProvider gui;

	public AboutSelectionListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		MessageBox messageBox = new MessageBox(gui.getShell(), SWT.ICON_INFORMATION);
		messageBox.setText(Resources.get("msg.about.title"));
		messageBox.setMessage(Resources.get("msg.about.body", Version.getInstance().getNumber(), Version.getInstance().getDate()) + " - " + Resources.get("msg.about.site") + NewLine.SYSTEM_LINE_SEPARATOR + Resources.get("msg.about.icon"));
		messageBox.open();
	}

}
