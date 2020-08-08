package it.albertus.acodec.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

import it.albertus.acodec.engine.CodecMode;
import it.albertus.acodec.gui.CodecGui;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModeRadioSelectionListener extends SelectionAdapter {

	private final CodecGui gui;
	private final Button radio;
	private final CodecMode mode;

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (radio.getSelection()) {
			gui.getConfig().setMode(mode);
			gui.getInputText().notifyListeners(SWT.Modify, null);
		}
	}

}
