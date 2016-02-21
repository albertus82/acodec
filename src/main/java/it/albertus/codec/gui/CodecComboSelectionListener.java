package it.albertus.codec.gui;

import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.engine.CodecType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class CodecComboSelectionListener extends SelectionAdapter {

	private final CodecEngine engine;
	private final Combo codecCombo;
	private final Text inputText;

	public CodecComboSelectionListener(CodecEngine engine, Combo codecCombo, Text inputText) {
		this.engine = engine;
		this.codecCombo = codecCombo;
		this.inputText = inputText;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		engine.setCodec(CodecType.values()[(codecCombo.getSelectionIndex())]);
		inputText.notifyListeners(SWT.Modify, null);
	}

}
