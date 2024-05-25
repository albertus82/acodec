package io.github.albertus82.acodec.gui.listener;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Text;

public class TextSelectAllMenuListener extends TextListener implements SelectionListener {

	public TextSelectAllMenuListener(final Text text) {
		super(text);
	}

	@Override
	public void widgetSelected(final SelectionEvent e) {
		text.selectAll();
	}

	@Override
	public void widgetDefaultSelected(final SelectionEvent e) {/* Ignore */}

}
