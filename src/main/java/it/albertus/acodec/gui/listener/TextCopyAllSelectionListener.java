package it.albertus.acodec.gui.listener;

import java.util.function.Supplier;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;

import it.albertus.jface.closeable.CloseableClipboard;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TextCopyAllSelectionListener extends SelectionAdapter {

	private final Supplier<Text> textSupplier;

	@Override
	public void widgetSelected(final SelectionEvent e) {
		final Text text = textSupplier.get();
		if (text != null && !text.isDisposed()) {
			try (final CloseableClipboard cc = new CloseableClipboard(new Clipboard(text.getDisplay()))) {
				cc.getClipboard().setContents(new String[] { text.getText() }, new Transfer[] { TextTransfer.getInstance() });
			}
		}
	}

}
