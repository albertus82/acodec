package it.albertus.acodec.common.resources;

import lombok.NonNull;

public interface ConfigurableMessages extends Messages {

	Language getLanguage();

	default void setLanguage(@NonNull final Language language) {
		setLanguage(language.getLocale().getLanguage());
	}

	void setLanguage(@NonNull String language);

}
