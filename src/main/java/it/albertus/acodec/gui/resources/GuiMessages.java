package it.albertus.acodec.gui.resources;

import java.util.Locale;

import it.albertus.acodec.common.resources.CommonMessages;
import it.albertus.acodec.common.resources.ConfigurableMessages;
import it.albertus.acodec.common.resources.Language;
import it.albertus.acodec.common.resources.MessageBundle;
import lombok.NonNull;

public enum GuiMessages implements ConfigurableMessages {

	INSTANCE;

	private static final ConfigurableMessages fallbackMessages = CommonMessages.INSTANCE;

	private final MessageBundle bundle = new MessageBundle(getClass().getName().toLowerCase(Locale.ROOT));

	@Override
	public String get(@NonNull final String key) {
		return bundle.getMessage(key, fallbackMessages::get);
	}

	@Override
	public String get(@NonNull final String key, @NonNull final Object... params) {
		return bundle.getMessage(key, params, fallbackMessages::get);
	}

	@Override
	public Language getLanguage() {
		return bundle.getLanguage();
	}

	@Override
	public void setLanguage(@NonNull final Language language) { // NOSONAR Enum singleton
		bundle.setLanguage(language, fallbackMessages::setLanguage);
	}

}
