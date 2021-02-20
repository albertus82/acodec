package it.albertus.acodec.common.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Widget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	class Defaults {
		public static String get(final String key, final ResourceBundle resourceBundle, final Supplier<String> fallbackSupplier) {
			String message;
			try {
				message = resourceBundle.getString(key);
				message = message != null ? message.replace("''", "'").trim() : "";
			}
			catch (final MissingResourceException e) {
				message = fallbackSupplier.get();
			}
			return message;
		}

		public static String get(final String key, final Object[] params, final ResourceBundle resourceBundle, final Supplier<String> fallbackSupplier) {
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
				message = fallbackSupplier.get();
			}
			return message;
		}
	}

}
