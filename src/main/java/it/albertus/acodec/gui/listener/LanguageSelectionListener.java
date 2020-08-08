package it.albertus.acodec.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.acodec.gui.CodecGui;
import it.albertus.acodec.resources.Messages;
import it.albertus.acodec.resources.Messages.Language;

public class LanguageSelectionListener extends SelectionAdapter {

	private final CodecGui gui;

	public LanguageSelectionListener(final CodecGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		final MenuItem languageMenuItem = (MenuItem) e.widget;
		if (languageMenuItem.getSelection() && !Messages.getLanguage().equals(languageMenuItem.getData())) {
			gui.setLanguage((Language) languageMenuItem.getData());
		}
	}

}
