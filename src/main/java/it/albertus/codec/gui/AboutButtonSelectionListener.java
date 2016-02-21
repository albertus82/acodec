package it.albertus.codec.gui;

import it.albertus.codec.resources.Resources;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class AboutButtonSelectionListener extends SelectionAdapter {

	private final Shell shell;

	public AboutButtonSelectionListener(final Shell shell) {
		this.shell = shell;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_INFORMATION);
		messageBox.setText(Resources.get("msg.about.title"));
		messageBox.setMessage(Resources.get("msg.about.body", Version.getInstance().getNumber(), Version.getInstance().getDate()) + " - " + Resources.get("msg.about.site") + NewLine.SYSTEM_LINE_SEPARATOR + Resources.get("msg.about.icon"));
		messageBox.open();
	}

}
