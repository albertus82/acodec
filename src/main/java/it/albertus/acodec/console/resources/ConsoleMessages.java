package it.albertus.acodec.console.resources;

import java.util.ResourceBundle;

import it.albertus.acodec.common.resources.CommonMessages;
import it.albertus.acodec.common.resources.Messages;
import it.albertus.acodec.common.resources.internal.MessagesImpl;
import lombok.NonNull;

public enum ConsoleMessages implements Messages {

	INSTANCE;

	private static final Messages fallbackMessages = CommonMessages.INSTANCE;

	private final MessagesImpl impl = new MessagesImpl(ResourceBundle.getBundle(getClass().getName().toLowerCase(), ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)));

	@Override
	public String get(@NonNull final String key) {
		return impl.get(key, fallbackMessages::get);
	}

	@Override
	public String get(@NonNull final String key, final Object... params) {
		return impl.get(key, params, fallbackMessages::get);
	}

}
