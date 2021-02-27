package it.albertus.acodec.common.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class MessageBundle {

	@NonNull
	private ResourceBundle resourceBundle;

	public Language getLanguage() {
		for (final Language language : Language.values()) {
			if (language.getLocale().getLanguage().equals(resourceBundle.getLocale().getLanguage())) {
				return language;
			}
		}
		return Language.ENGLISH; // Default.
	}

	public void setLanguage(@NonNull final Language language, final Consumer<Language> fallbackConsumer) {
		resourceBundle = ResourceBundle.getBundle(resourceBundle.getBaseBundleName(), language.getLocale(), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
		if (fallbackConsumer != null) {
			fallbackConsumer.accept(language);
		}
	}

	public String getMessage(@NonNull final String key, final UnaryOperator<String> fallbackFunction) {
		String message;
		try {
			message = resourceBundle.getString(key);
			message = message != null ? message.replace("''", "'").trim() : "";
		}
		catch (final MissingResourceException e) {
			if (fallbackFunction != null) {
				message = fallbackFunction.apply(key);
			}
			else {
				log.log(Level.WARNING, e, () -> "No message found with key \"" + key + "\"!");
				message = key;
			}
		}
		return message;
	}

	public String getMessage(@NonNull final String key, @NonNull final Object[] params, final BiFunction<String, Object[], String> fallbackFunction) {
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
			if (fallbackFunction != null) {
				message = fallbackFunction.apply(key, params);
			}
			else {
				log.log(Level.WARNING, e, () -> "No message found with key \"" + key + "\"!");
				message = key;
			}
		}
		return message;
	}

}
