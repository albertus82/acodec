package it.albertus.acodec.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

import it.albertus.acodec.common.engine.CodecMode;
import it.albertus.acodec.gui.CodecGui;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModeRadioSelectionListener extends SelectionAdapter {

	@NonNull
	private final CodecGui gui;
	@NonNull
	private final Button radio;
	@NonNull
	private final CodecMode mode;

	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (radio.getSelection()) {
			gui.setMode(mode);
			gui.evaluateInputText();
		}
	}

}
