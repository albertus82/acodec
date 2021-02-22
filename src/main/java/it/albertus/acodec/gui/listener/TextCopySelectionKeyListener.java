package it.albertus.acodec.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

import it.albertus.jface.SwtUtils;
import it.albertus.jface.closeable.CloseableClipboard;

public enum TextCopySelectionKeyListener implements KeyListener {

	INSTANCE;

	@Override
	public void keyPressed(final KeyEvent e) {
		if (e.stateMask == SWT.MOD1 && e.keyCode == SwtUtils.KEY_COPY && e.widget instanceof Text && !e.widget.isDisposed()) {
			final Text text = (Text) e.widget;
			final String selectionText = text.getSelectionText();
			if (!selectionText.isEmpty()) {
				try (final CloseableClipboard cc = new CloseableClipboard(new Clipboard(text.getDisplay()))) {
					cc.getClipboard().setContents(new String[] { selectionText }, new Transfer[] { TextTransfer.getInstance() });
				}
			}
			e.doit = false; // Avoid the error message when copying from a password field.
		}
	}

	@Override
	public void keyReleased(final KeyEvent e) { /* Ignore */ }

}
