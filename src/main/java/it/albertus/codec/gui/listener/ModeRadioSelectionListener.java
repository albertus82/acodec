package it.albertus.codec.gui.listener;

import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.gui.CodecGui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

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
