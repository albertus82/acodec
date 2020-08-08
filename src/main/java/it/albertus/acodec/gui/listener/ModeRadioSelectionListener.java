package it.albertus.acodec.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

import it.albertus.acodec.engine.CodecMode;
import it.albertus.acodec.gui.CodecGui;

public class ModeRadioSelectionListener extends SelectionAdapter {

	private final CodecGui gui;
	private final CodecMode mode;
	private final Button radio;

	public ModeRadioSelectionListener(final CodecGui gui, final Button radio, final CodecMode mode) {
		this.gui = gui;
		this.radio = radio;
		this.mode = mode;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		if (radio.getSelection()) {
			gui.getEngine().setMode(mode);
			gui.getInputText().notifyListeners(SWT.Modify, null);
		}
	}

}
