package io.github.albertus82.acodec.gui.listener;

import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.MenuItem;

import io.github.albertus82.acodec.common.resources.ConfigurableMessages;
import io.github.albertus82.acodec.common.resources.Language;
import io.github.albertus82.acodec.gui.resources.GuiMessages;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LanguageSelectionListener extends SelectionAdapter {

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	@NonNull
	private final Consumer<Language> consumer;

	@Override
	public void widgetSelected(@NonNull final SelectionEvent event) {
		if (event.widget instanceof MenuItem) {
			final MenuItem languageMenuItem = (MenuItem) event.widget;
			if (languageMenuItem.getSelection()) {
				final Object data = languageMenuItem.getData(Language.class.getName());
				if (data instanceof Language && !data.equals(messages.getLanguage())) {
					consumer.accept((Language) data);
				}
			}
		}
	}

}
