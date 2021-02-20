package it.albertus.acodec.gui.resources;

import java.util.Locale;
import java.util.ResourceBundle;

import it.albertus.acodec.common.resources.CommonMessages;
import it.albertus.acodec.common.resources.ConfigurableMessages;

public enum GuiMessages implements ConfigurableMessages {

	INSTANCE;

	private static final ConfigurableMessages commonMessages = CommonMessages.INSTANCE;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle(getClass().getName().toLowerCase(), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));

	/** Aggiorna la lingua in cui vengono mostrati i messaggi. */
	@Override
	public void setLanguage(final String language) { // NOSONAR Enum singleton
		if (language != null) {
			resourceBundle = ResourceBundle.getBundle(resourceBundle.getBaseBundleName(), new Locale(language), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
			commonMessages.setLanguage(language);
		}
	}

	@Override
	public Language getLanguage() {
		return ConfigurableMessagesDefaults.getLanguage(resourceBundle);
	}

	@Override
	public String get(final String key) {
		return MessagesDefaults.get(key, resourceBundle, () -> commonMessages.get(key));
	}

	@Override
	public String get(final String key, final Object... params) {
		return MessagesDefaults.get(key, params, resourceBundle, () -> commonMessages.get(key, params));
	}

}
