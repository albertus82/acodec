package io.github.albertus82.acodec.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

import io.github.albertus82.jface.SwtUtils;
import lombok.NonNull;

public class TextSelectAllKeyListener extends TextListener implements KeyListener {

	public TextSelectAllKeyListener(final Text text) {
		super(text);
	}

	@Override
	public void keyPressed(@NonNull final KeyEvent e) {
		if (e.stateMask == SWT.MOD1 && e.keyCode == SwtUtils.KEY_SELECT_ALL) {
			selectAll();
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {/* Ignore */}

}
