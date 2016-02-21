package it.albertus.codec.gui;

import it.albertus.codec.engine.CodecEngine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;

public class InputTextModifyListener implements ModifyListener {

	private static final String ERROR_SUFFIX = " --";
	private static final String ERROR_PREFIX = "-- ";

	private final CodecEngine engine;
	private final Text input;
	private final Text output;
	private final Color defaultTextColor;
	private final Color inactiveTextColor;

	public InputTextModifyListener(final CodecEngine engine, final Text input, final Text output) {
		this.engine = engine;
		this.input = input;
		this.output = output;
		this.defaultTextColor = output.getForeground();
		this.inactiveTextColor = output.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	}

	@Override
	public void modifyText(final ModifyEvent event) {
		String result = null;
		try {
			result = engine.run(input.getText());
		}
		catch (Exception e) {
			print(e.getMessage(), true);
			return;
		}
		print(result, false);
	}

	private void print(String text, final boolean error) {
		if (error) {
			text = ERROR_PREFIX + text + ERROR_SUFFIX;
			if (!output.getForeground().equals(inactiveTextColor)) {
				output.setForeground(inactiveTextColor);
			}
		}
		else {
			if (!output.getForeground().equals(defaultTextColor)) {
				output.setForeground(defaultTextColor);
			}
		}
		output.setText(text);
	}

}
