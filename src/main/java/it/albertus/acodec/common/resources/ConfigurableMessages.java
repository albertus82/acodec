package it.albertus.acodec.common.resources;

import java.util.ResourceBundle;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

public interface ConfigurableMessages extends Messages {

	Language getLanguage();

	void setLanguage(String language);

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	class ConfigurableMessagesDefaults extends MessagesDefaults {
		public static Language getLanguage(final ResourceBundle resourceBundle) {
			for (final Language language : Language.values()) {
				if (language.getLocale().equals(resourceBundle.getLocale())) {
					return language;
				}
			}
			return Language.ENGLISH; // Default.
		}
	}

}
