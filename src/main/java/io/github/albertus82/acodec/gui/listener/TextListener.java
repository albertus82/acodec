package io.github.albertus82.acodec.gui.listener;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Text;

import io.github.albertus82.jface.closeable.CloseableClipboard;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class TextListener {

	@NonNull
	protected final Text text;

	protected void copySelection() {
		if (!text.isDisposed()) {
			final String selectionText = text.getSelectionText();
			if (!selectionText.isEmpty()) {
				try (final CloseableClipboard cc = new CloseableClipboard(new Clipboard(text.getDisplay()))) {
					cc.getClipboard().setContents(new String[] { selectionText }, new Transfer[] { TextTransfer.getInstance() });
				}
			}
		}
	}

	protected void selectAll() {
		if (!text.isDisposed()) {
			text.selectAll();
		}
	}

}
