package io.github.albertus82.acodec.common.resources;

import java.util.Locale;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {

	ENGLISH(Locale.ENGLISH),
	ITALIAN(Locale.ITALIAN);

	private final Locale locale;

	public static Optional<Language> fromString(final String lang) {
		for (final Language e : Language.values()) {
			if (e.locale.getLanguage().equals(new Locale(lang).getLanguage())) {
				return Optional.of(e);
			}
		}
		return Optional.empty();
	}

}
