package io.github.albertus82.acodec.common.resources;

import java.util.Locale;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {

	ENGLISH(Locale.ENGLISH),
	ITALIAN(Locale.ITALIAN);

	private final Locale locale;

}
