package it.albertus.codec.gui;

import it.albertus.codec.Codec;
import it.albertus.codec.Codec.Type;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class CodecComboSelectionListener extends SelectionAdapter {

	private final Codec app;
	private final Combo codecCombo;
	private final Text inputText;

	public CodecComboSelectionListener(Codec app, Combo codecCombo, Text inputText) {
		this.app = app;
		this.codecCombo = codecCombo;
		this.inputText = inputText;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		app.setCodec(Type.values()[(codecCombo.getSelectionIndex())]);
		inputText.notifyListeners(SWT.Modify, null);
	}

}
