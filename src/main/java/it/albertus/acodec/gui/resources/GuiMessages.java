package it.albertus.acodec.gui.resources;

import java.util.ResourceBundle;

import it.albertus.acodec.common.resources.CommonMessages;
import it.albertus.acodec.common.resources.ConfigurableMessages;
import it.albertus.acodec.common.resources.Language;
import it.albertus.acodec.common.resources.internal.MessageBundle;
import lombok.NonNull;

public enum GuiMessages implements ConfigurableMessages {

	INSTANCE;

	private static final ConfigurableMessages fallbackMessages = CommonMessages.INSTANCE;

	private final MessageBundle bundle = new MessageBundle(ResourceBundle.getBundle(getClass().getName().toLowerCase(), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)));

	@Override
	public Language getLanguage() {
		return bundle.getLanguage();
	}

	@Override
	public void setLanguage(@NonNull final Language language) { // NOSONAR Enum singleton
		bundle.setLanguage(language, fallbackMessages::setLanguage);
	}

	@Override
	public String get(@NonNull final String key) {
		return bundle.get(key, fallbackMessages::get);
	}

	@Override
	public String get(@NonNull final String key, final Object... params) {
		return bundle.get(key, params, fallbackMessages::get);
	}

}
