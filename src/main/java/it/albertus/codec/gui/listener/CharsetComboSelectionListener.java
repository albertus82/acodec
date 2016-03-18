package it.albertus.codec.gui.listener;

import it.albertus.codec.gui.CodecGui;

import java.nio.charset.Charset;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class CharsetComboSelectionListener extends SelectionAdapter {

	private final CodecGui gui;

	public CharsetComboSelectionListener(final CodecGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		final String charsetName = gui.getCharsetCombo().getText();
		gui.getEngine().setCharset(Charset.forName(charsetName));
		gui.getInputText().notifyListeners(SWT.Modify, null);
	}

}
