package it.albertus.acodec.common.resources;

public interface ConfigurableMessages extends Messages {

	Language getLanguage();

	void setLanguage(String language);

}
