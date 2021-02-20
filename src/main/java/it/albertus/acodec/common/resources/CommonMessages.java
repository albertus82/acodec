package it.albertus.acodec.common.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
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
		String message;
		try {
			message = resourceBundle.getString(key);
			message = message != null ? message.replace("''", "'").trim() : "";
		}
		catch (final MissingResourceException e) {
			message = JFaceMessages.get(key);
		}
		return message;
	}

	@Override
	public String get(final String key, final Object... params) {
		final List<String> stringParams = new ArrayList<>(params.length);
		for (final Object param : params) {
			stringParams.add(String.valueOf(param));
		}
		String message;
		try {
			message = MessageFormat.format(resourceBundle.getString(key), stringParams.toArray());
			message = message != null ? message.trim() : "";
		}
		catch (final MissingResourceException e) {
			message = JFaceMessages.get(key, params);
		}
		return message;
	}

}
