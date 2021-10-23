package io.github.albertus82.acodec.gui.listener;

import java.nio.charset.Charset;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import io.github.albertus82.acodec.gui.CodecGui;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CharsetComboSelectionListener extends SelectionAdapter {

	@NonNull
	private final CodecGui gui;

	@Override
	public void widgetSelected(final SelectionEvent event) {
		final String charsetName = gui.getCharsetCombo().getText();
		gui.setCharset(Charset.forName(charsetName));
		gui.evaluateInputText();
	}

}
