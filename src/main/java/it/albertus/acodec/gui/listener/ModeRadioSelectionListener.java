package it.albertus.acodec.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

import it.albertus.acodec.common.engine.CodecMode;
import it.albertus.acodec.gui.CodecGui;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModeRadioSelectionListener extends SelectionAdapter {

	private final CodecGui gui;
	private final Button radio;
	private final CodecMode mode;

	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (radio.getSelection()) {
			gui.setMode(mode);
			gui.evaluateInputText();
		}
	}

}
