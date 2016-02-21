package it.albertus.codec.gui;

import it.albertus.codec.Codec;
import it.albertus.codec.Codec.Mode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class ModeRadioSelectionListener extends SelectionAdapter {

	private final Codec app;
	private final Button radio;
	private final Mode mode;
	private final Text inputText;

	public ModeRadioSelectionListener(Codec app, Button radio, Mode mode, Text inputText) {
		this.app = app;
		this.radio = radio;
		this.mode = mode;
		this.inputText = inputText;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (radio.getSelection()) {
			app.setMode(mode);
			inputText.notifyListeners(SWT.Modify, null);
		}
	}

}
