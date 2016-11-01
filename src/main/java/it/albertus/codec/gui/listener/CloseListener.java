package it.albertus.codec.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class CloseListener extends SelectionAdapter implements Listener {

	private final IShellProvider gui;

	public CloseListener(final IShellProvider gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		gui.getShell().dispose();
	}

	@Override
	public void handleEvent(final Event event) {
		widgetSelected(null);
	}

}
