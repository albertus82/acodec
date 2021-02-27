package it.albertus.acodec.common.resources;

import java.util.ResourceBundle;

import it.albertus.acodec.common.resources.internal.MessageBundle;
import it.albertus.jface.JFaceMessages;
import lombok.NonNull;

public enum CommonMessages implements ConfigurableMessages {

	INSTANCE;

	private static final ConfigurableMessages fallbackMessages = FallbackMessages.INSTANCE;

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

	private enum FallbackMessages implements ConfigurableMessages {

		INSTANCE;

		@Override
		public String get(@NonNull final String key) {
			return JFaceMessages.get(key);
		}

		@Override
		public String get(@NonNull final String key, final Object... params) {
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

	}

}
