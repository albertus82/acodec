package it.albertus.acodec.gui.listener;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.acodec.common.resources.ConfigurableMessages;
import it.albertus.acodec.common.resources.Language;
import it.albertus.acodec.gui.CodecGui;
import it.albertus.acodec.gui.resources.GuiMessages;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LanguageSelectionListener extends SelectionAdapter {

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private final CodecGui gui;

	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (event.widget instanceof MenuItem) {
			final MenuItem languageMenuItem = (MenuItem) event.widget;
			if (languageMenuItem.getSelection() && !messages.getLanguage().equals(languageMenuItem.getData())) {
				gui.setLanguage((Language) languageMenuItem.getData());
			}
		}
	}

}
