package it.albertus.codec.gui;

import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.engine.CodecType;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class CodecComboSelectionListener extends SelectionAdapter {

	private final CodecEngine engine;
	private final Combo codecCombo;
	private final Text inputText;
	private final Map<CodecMode, Button> modeRadios;

	public CodecComboSelectionListener(CodecEngine engine, Combo codecCombo, Text inputText, Map<CodecMode, Button> modeRadios) {
		this.engine = engine;
		this.codecCombo = codecCombo;
		this.inputText = inputText;
		this.modeRadios = modeRadios;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		CodecType codec = CodecType.values()[(codecCombo.getSelectionIndex())];
		engine.setCodec(codec);

		/* Gestione radio */
		Button encodeRadio = modeRadios.get(CodecMode.ENCODE);
		Button decodeRadio = modeRadios.get(CodecMode.DECODE);

		if (codec.getModes().contains(CodecMode.DECODE)) {
			if (!decodeRadio.getEnabled()) {
				decodeRadio.setEnabled(true);
			}
		}
		else {
			if (decodeRadio.getEnabled()) {
				decodeRadio.setSelection(false);
				decodeRadio.setEnabled(false);
				encodeRadio.setSelection(true);
				encodeRadio.notifyListeners(SWT.Selection, null);
			}
		}

		inputText.notifyListeners(SWT.Modify, null);
	}

}
