package it.albertus.codec.gui;

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
		messageBox.setText("About Codec");
		messageBox.setMessage("Version " + Version.getInstance().getNumber() + " (" + Version.getInstance().getDate() + ')' + NewLine.CRLF + "Icon by www.aha-soft.com");
		messageBox.open();
	}

}
