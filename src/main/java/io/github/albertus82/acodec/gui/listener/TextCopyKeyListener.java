package io.github.albertus82.acodec.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

import io.github.albertus82.jface.SwtUtils;
import lombok.NonNull;

public class TextCopyKeyListener extends TextListener implements KeyListener {

	public TextCopyKeyListener(final Text text) {
		super(text);
	}

	@Override
	public void keyPressed(@NonNull final KeyEvent e) {
		if (e.stateMask == SWT.MOD1 && e.keyCode == SwtUtils.KEY_COPY) {
			e.doit = false; // Avoid the error message when copying from a password field.
			copySelection();
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {/* Ignore */}

}
