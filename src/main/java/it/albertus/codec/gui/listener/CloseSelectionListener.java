package it.albertus.codec.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CloseSelectionListener extends SelectionAdapter {

	private final IShellProvider gui;

	public CloseSelectionListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		gui.getShell().dispose();
	}

}
