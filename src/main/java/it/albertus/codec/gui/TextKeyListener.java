package it.albertus.codec.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;

public class TextKeyListener extends KeyAdapter {

	private final Text text;

	public TextKeyListener(final Text text) {
		this.text = text;
	}

	@Override
	public void keyPressed(final KeyEvent e) {
		// Supporto CTRL+A per "Seleziona tutto"...
		if (e.stateMask == SWT.MOD1 && e.keyCode == Utils.KEY_SELECT_ALL) {
			text.selectAll();
		}
	}

}
