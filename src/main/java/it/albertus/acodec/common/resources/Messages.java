package it.albertus.acodec.common.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Level;

import org.eclipse.swt.widgets.Widget;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

public interface Messages {

	String get(@NonNull String key);

	String get(@NonNull String key, Object... params);

	default String get(@NonNull final Widget widget) {
		return get(String.valueOf(widget.getData()));
	}

	@Log
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	class Defaults {
		public static String get(@NonNull final String key, @NonNull final ResourceBundle resourceBundle) {
			return get(key, resourceBundle, null);
		}

		public static String get(@NonNull final String key, @NonNull final ResourceBundle resourceBundle, final Supplier<String> fallbackSupplier) {
			String message;
			try {
				message = resourceBundle.getString(key);
				message = message != null ? message.replace("''", "'").trim() : "";
			}
			catch (final MissingResourceException e) {
				if (fallbackSupplier != null) {
					message = fallbackSupplier.get();
				}
				else {
					log.log(Level.WARNING, e, () -> "No message found with key \"" + key + "\"!");
					message = key;
				}
			}
			return message;
		}

		public static String get(@NonNull final String key, @NonNull final Object[] params, @NonNull final ResourceBundle resourceBundle) {
			return get(key, params, resourceBundle, null);
		}

		public static String get(@NonNull final String key, @NonNull final Object[] params, @NonNull final ResourceBundle resourceBundle, final Supplier<String> fallbackSupplier) {
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
				if (fallbackSupplier != null) {
					message = fallbackSupplier.get();
				}
				else {
					log.log(Level.WARNING, e, () -> "No message found with key \"" + key + "\"!");
					message = key;
				}
			}
			return message;
		}
	}

	@Getter
	@RequiredArgsConstructor
	enum Language {
		ENGLISH(Locale.ENGLISH),
		ITALIAN(Locale.ITALIAN);

		private final Locale locale;
	}

}
