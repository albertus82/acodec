package it.albertus.acodec.common.resources;

import java.util.Collection;

import it.albertus.jface.JFaceMessages;
import lombok.NonNull;

public enum FallbackMessages implements ConfigurableMessages {

	INSTANCE;

	@Override
	public String get(@NonNull final String key) {
		return JFaceMessages.get(key);
	}

	@Override
	public String get(@NonNull final String key, @NonNull final Object... params) {
		return JFaceMessages.get(key, params);
	}

	@Override
	public Language getLanguage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLanguage(@NonNull final Language language) { // NOSONAR Enum singleton
		JFaceMessages.setLanguage(language.getLocale().getLanguage());
	}

	@Override
	public Collection<String> getKeys() {
		return JFaceMessages.getKeys();
	}

}
