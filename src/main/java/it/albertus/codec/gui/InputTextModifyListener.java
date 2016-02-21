package it.albertus.codec.gui;

import it.albertus.codec.Codec;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;

public class InputTextModifyListener implements ModifyListener {

	private final Codec app;
	private final Text input;
	private final Text output;
	private final Color defaultTextColor;
	private final Color inactiveTextColor;

	public InputTextModifyListener(Codec app, Text input, Text output) {
		this.app = app;
		this.input = input;
		this.output = output;
		this.defaultTextColor = output.getForeground();
		this.inactiveTextColor = output.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	}

	@Override
	public void modifyText(ModifyEvent e) {
		String result = app.encode(input.getText());
		if (result == null) {
			if (!output.getForeground().equals(inactiveTextColor)) {
				output.setForeground(inactiveTextColor);
			}
			output.setText("-- Select codec --");
		}
		else {
			if (!output.getForeground().equals(defaultTextColor)) {
				output.setForeground(defaultTextColor);
			}
			output.setText(result);
		}
	}

}
