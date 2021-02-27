package it.albertus.acodec.common.resources;

import org.eclipse.swt.widgets.Widget;

import lombok.NonNull;

public interface Messages {

	String get(@NonNull String key);

	String get(@NonNull String key, Object... params);

	default String get(@NonNull final Widget widget) {
		return get(String.valueOf(widget.getData()));
	}

}
