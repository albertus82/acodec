package io.github.albertus82.acodec.common.resources;

import lombok.NonNull;

public interface ConfigurableMessages extends Messages {

	Language getLanguage();

	void setLanguage(@NonNull final Language language);

}
