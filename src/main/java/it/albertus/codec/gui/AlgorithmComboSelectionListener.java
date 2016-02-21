package it.albertus.codec.gui;

import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.engine.CodecAlgorithm;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

public class AlgorithmComboSelectionListener extends SelectionAdapter {

	private final CodecEngine engine;
	private final Combo algorithmCombo;
	private final Text inputText;
	private final Map<CodecMode, Button> modeRadios;

	public AlgorithmComboSelectionListener(final CodecEngine engine, final Combo algorithmCombo, final Text inputText, final Map<CodecMode, Button> modeRadios) {
		this.engine = engine;
		this.algorithmCombo = algorithmCombo;
		this.inputText = inputText;
		this.modeRadios = modeRadios;
	}

	@Override
	public void widgetSelected(final SelectionEvent se) {
		CodecAlgorithm algorithm = CodecAlgorithm.values()[(algorithmCombo.getSelectionIndex())];
		engine.setAlgorithm(algorithm);

		/* Gestione radio */
		Button encodeRadio = modeRadios.get(CodecMode.ENCODE);
		Button decodeRadio = modeRadios.get(CodecMode.DECODE);

		if (algorithm.getModes().contains(CodecMode.DECODE)) {
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
