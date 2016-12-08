package it.albertus.codec.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;

import it.albertus.codec.gui.CodecGui;

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
		String result = null;
		if (gui.isDirty()) {
			gui.setDirty(false);
			gui.getInputText().setText("");
		}
		try {
			result = gui.getEngine().run(gui.getInputText().getText());
		}
		catch (Exception e) {
			print(e.getMessage(), true);
			return;
		}
		print(result, false);
	}

	private void print(String text, final boolean error) {
		if (defaultTextColor == null) { // Fix color issues on some Linux GUIs
			defaultTextColor = gui.getOutputText().getForeground();
		}
		if (error) {
			text = ERROR_PREFIX + text + ERROR_SUFFIX;
			if (!gui.getOutputText().getForeground().equals(inactiveTextColor)) {
				gui.getOutputText().setForeground(inactiveTextColor);
			}
		}
		else {
			if (!gui.getOutputText().getForeground().equals(defaultTextColor)) {
				gui.getOutputText().setForeground(defaultTextColor);
			}
		}
		gui.getOutputText().setText(text != null ? text : "");
	}

}
