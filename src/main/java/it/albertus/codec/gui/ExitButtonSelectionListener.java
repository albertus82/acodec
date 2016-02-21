package it.albertus.codec.gui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;

public class ExitButtonSelectionListener extends SelectionAdapter {

	private final Shell shell;

	public ExitButtonSelectionListener(final Shell shell) {
		this.shell = shell;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		shell.dispose();
	}

}
