package it.albertus.acodec.console.resources;

import java.util.Locale;
import java.util.ResourceBundle;

import it.albertus.acodec.common.resources.CommonMessages;
import it.albertus.acodec.common.resources.Messages;

public enum ConsoleMessages implements Messages {

	INSTANCE;

	private static final CommonMessages commonMessages = CommonMessages.INSTANCE;

	private final String baseName = ConsoleMessages.class.getName().toLowerCase();

	private ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));

	/** Aggiorna la lingua in cui vengono mostrati i messaggi. */
	public void setLanguage(final String language) { // NOSONAR Enum singleton
		if (language != null) {
			resourceBundle = ResourceBundle.getBundle(baseName, new Locale(language), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
			commonMessages.setLanguage(language);
		}
	}

	public Language getLanguage() {
		for (final Language language : Language.values()) {
			if (language.getLocale().equals(resourceBundle.getLocale())) {
				return language;
			}
		}
		return Language.ENGLISH; // Default.
	}

	@Override
	public String get(final String key) {
		return Defaults.get(key, resourceBundle, () -> commonMessages.get(key));
	}

	@Override
	public String get(final String key, final Object... params) {
		return Defaults.get(key, params, resourceBundle, () -> commonMessages.get(key, params));
	}

}
