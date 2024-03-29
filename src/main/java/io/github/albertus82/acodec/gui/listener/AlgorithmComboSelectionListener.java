package io.github.albertus82.acodec.gui.listener;

import static io.github.albertus82.acodec.common.engine.CodecMode.DECODE;
import static io.github.albertus82.acodec.common.engine.CodecMode.ENCODE;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;

import io.github.albertus82.acodec.common.engine.CodecAlgorithm;
import io.github.albertus82.acodec.gui.CodecGui;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlgorithmComboSelectionListener extends SelectionAdapter {

	@NonNull
	private final CodecGui gui;

	@Override
	public void widgetSelected(final SelectionEvent event) {
		final int selectionIndex = gui.getAlgorithmCombo().getSelectionIndex();
		if (selectionIndex != -1) {
			final CodecAlgorithm algorithm = CodecAlgorithm.values()[selectionIndex];
			gui.setAlgorithm(algorithm);

			/* Enable drag and drop & Process file button */
			if (!gui.getProcessFileButton().getEnabled()) {
				gui.getProcessFileButton().setEnabled(true);
				gui.getShellDropTarget().setTransfer(new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance() }); // NOSONAR SWT v4.3.2 does not have the vararg.
			}

			/* Radios management */
			final Button encodeRadio = gui.getModeRadios().get(ENCODE);
			final Button decodeRadio = gui.getModeRadios().get(DECODE);

			if (algorithm.getModes().contains(DECODE)) {
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

			gui.evaluateInputText();
		}
	}

}
