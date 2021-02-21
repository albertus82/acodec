package it.albertus.acodec.common.resources;

import java.util.Locale;
import java.util.ResourceBundle;

import it.albertus.jface.JFaceMessages;
import lombok.NonNull;

public enum CommonMessages implements ConfigurableMessages {

	INSTANCE;

	private ResourceBundle resourceBundle = ResourceBundle.getBundle(getClass().getName().toLowerCase(), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));

	@Override
	public void setLanguage(@NonNull final String language) { // NOSONAR Enum singleton
		resourceBundle = ResourceBundle.getBundle(resourceBundle.getBaseBundleName(), new Locale(language), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
		JFaceMessages.setLanguage(language);
	}

	@Override
	public Language getLanguage() {
		for (final Language language : Language.values()) {
			if (language.getLocale().equals(resourceBundle.getLocale())) {
				return language;
			}
		}
		return Language.ENGLISH; // Default.
	}

	@Override
	public String get(@NonNull final String key) {
		return Defaults.get(key, resourceBundle, () -> JFaceMessages.get(key));
	}

	@Override
	public String get(@NonNull final String key, final Object... params) {
		return Defaults.get(key, params, resourceBundle, () -> JFaceMessages.get(key, params));
	}

}
