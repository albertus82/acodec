package it.albertus.acodec.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;

import it.albertus.acodec.engine.StringCodec;
import it.albertus.acodec.gui.CodecGui;

public class InputTextModifyListener implements ModifyListener {

	private static final String ERROR_SUFFIX = " --";
	private static final String ERROR_PREFIX = "-- ";

	private final CodecGui gui;
	private final Color inactiveTextColor;
	private Color defaultTextColor;

	public InputTextModifyListener(final CodecGui gui) {
		this.gui = gui;
		this.inactiveTextColor = gui.getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	}

	@Override
	public void modifyText(final ModifyEvent event) {
		String result;
		if (gui.isDirty()) {
			gui.setDirty(false);
			gui.getInputText().setText("");
		}
		try {
			result = new StringCodec(gui.getConfig()).run(gui.getInputText().getText());
		}
		catch (final Exception e) {
			print(e);
			return;
		}
		print(result, false);
	}

	private void print(final Exception e) {
		print(e.getMessage(), true);
	}

	private void print(final String text, final boolean error) {
		String outputText = text != null ? text : "";
		if (defaultTextColor == null) { // Fix color issues on some Linux GUIs
			defaultTextColor = gui.getOutputText().getForeground();
		}
		if (error) {
			outputText = new StringBuilder(outputText).insert(0, ERROR_PREFIX).append(ERROR_SUFFIX).toString();
			if (!gui.getOutputText().getForeground().equals(inactiveTextColor)) {
				gui.getOutputText().setForeground(inactiveTextColor);
			}
		}
		else {
			if (!gui.getOutputText().getForeground().equals(defaultTextColor)) {
				gui.getOutputText().setForeground(defaultTextColor);
			}
		}
		gui.getOutputText().setText(outputText);
	}

}
