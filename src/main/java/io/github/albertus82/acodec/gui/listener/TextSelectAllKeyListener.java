package io.github.albertus82.acodec.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

import it.albertus.jface.SwtUtils;
import lombok.NonNull;

public enum TextSelectAllKeyListener implements KeyListener {

	INSTANCE;

	@Override
	public void keyPressed(@NonNull final KeyEvent e) {
		if (e.stateMask == SWT.MOD1 && e.keyCode == SwtUtils.KEY_SELECT_ALL && e.widget instanceof Text && !e.widget.isDisposed()) {
			final Text text = (Text) e.widget;
			text.selectAll();
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) {/* Ignore */}

}
