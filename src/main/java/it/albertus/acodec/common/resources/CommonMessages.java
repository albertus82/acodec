package it.albertus.acodec.common.resources;

import java.util.Locale;
import java.util.ResourceBundle;

import it.albertus.jface.JFaceMessages;

public enum CommonMessages implements Messages {

	INSTANCE;

	private final String baseName = CommonMessages.class.getName().toLowerCase();

	private ResourceBundle resourceBundle = ResourceBundle.getBundle(baseName, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));

	/** Aggiorna la lingua in cui vengono mostrati i messaggi. */
	public void setLanguage(final String language) { // NOSONAR Enum singleton
		if (language != null) {
			resourceBundle = ResourceBundle.getBundle(baseName, new Locale(language), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
			JFaceMessages.setLanguage(language);
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
		return Defaults.get(key, resourceBundle, () -> JFaceMessages.get(key));
	}

	@Override
	public String get(final String key, final Object... params) {
		return Defaults.get(key, params, resourceBundle, () -> JFaceMessages.get(key, params));
	}

}
