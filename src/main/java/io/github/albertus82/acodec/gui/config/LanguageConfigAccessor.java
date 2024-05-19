package io.github.albertus82.acodec.gui.config;

import java.util.Locale;

import io.github.albertus82.acodec.common.resources.Language;
import io.github.albertus82.acodec.gui.preference.Preference;
import io.github.albertus82.jface.preference.IPreferencesConfiguration;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LanguageConfigAccessor {

	public static final String DEFAULT_LANGUAGE = Locale.getDefault().getLanguage();

	@NonNull
	private final IPreferencesConfiguration configuration;

	public @NonNull Language getLanguage() {
		return Language.fromString(configuration.getString(Preference.LANGUAGE, DEFAULT_LANGUAGE)).orElse(Language.ENGLISH);
	}

}
