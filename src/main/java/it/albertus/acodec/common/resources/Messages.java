package it.albertus.acodec.common.resources;

import java.util.Locale;

import org.eclipse.swt.widgets.Widget;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface Messages {

	@Getter
	@RequiredArgsConstructor
	public enum Language {

		ENGLISH(Locale.ENGLISH),
		ITALIAN(Locale.ITALIAN);

		private final Locale locale;

	}

	default String get(final Widget widget) {
		return get(widget.getData().toString());
	}

	String get(String key);

	String get(String key, Object... params);

}
