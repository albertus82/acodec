package it.albertus.codec.gui;

import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.engine.CodecMode;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class ModeRadioSelectionListener extends SelectionAdapter {

	private final CodecEngine engine;
	private final CodecMode mode;
	private final Button radio;
	private final Text inputText;

	public ModeRadioSelectionListener(CodecEngine engine, Button radio, CodecMode mode, Text inputText) {
		this.engine = engine;
		this.radio = radio;
		this.mode = mode;
		this.inputText = inputText;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (radio.getSelection()) {
			engine.setMode(mode);
			inputText.notifyListeners(SWT.Modify, null);
		}
	}

}
