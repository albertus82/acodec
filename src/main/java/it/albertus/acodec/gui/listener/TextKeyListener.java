package it.albertus.acodec.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;

import it.albertus.jface.SwtUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TextKeyListener extends KeyAdapter {

	private final Text text;

	@Override
	public void keyPressed(final KeyEvent e) {
		// Supporto CTRL+A per "Seleziona tutto"...
		if (e.stateMask == SWT.MOD1 && e.keyCode == SwtUtils.KEY_SELECT_ALL) {
			text.selectAll();
		}
	}

}