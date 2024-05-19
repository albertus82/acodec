package io.github.albertus82.acodec.gui.config;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.eclipse.jface.util.Util;

import io.github.albertus82.acodec.common.util.BuildInfo;
import io.github.albertus82.acodec.gui.resources.GuiMessages;
import io.github.albertus82.jface.preference.IPreferencesConfiguration;
import io.github.albertus82.jface.preference.PreferencesConfiguration;
import io.github.albertus82.util.InitializationException;
import io.github.albertus82.util.SystemUtils;
import io.github.albertus82.util.config.Configuration;
import io.github.albertus82.util.config.PropertiesConfiguration;

public class ApplicationConfig extends Configuration {

	private static final String DIRECTORY_NAME = Util.isLinux() ? '.' + BuildInfo.getProperty("project.artifactId") : BuildInfo.getProperty("project.name");

	public static final String APPDATA_DIRECTORY = SystemUtils.getOsSpecificLocalAppDataDir() + File.separator + DIRECTORY_NAME;

	private static final String CFG_FILE_NAME = (Util.isLinux() ? BuildInfo.getProperty("project.artifactId") : BuildInfo.getProperty("project.name").replace(" ", "")) + ".cfg";

	private static volatile ApplicationConfig instance; // NOSONAR Use a thread-safe type; adding "volatile" is not enough to make this field thread-safe. Use a thread-safe type; adding "volatile" is not enough to make this field thread-safe.
	private static volatile IPreferencesConfiguration wrapper; // NOSONAR Use a thread-safe type; adding "volatile" is not enough to make this field thread-safe. Use a thread-safe type; adding "volatile" is not enough to make this field thread-safe.
	private static int instanceCount = 0;

	private final LanguageConfigAccessor languageConfigAccessor;

	private ApplicationConfig() throws IOException {
		super(new PropertiesConfiguration(DIRECTORY_NAME + File.separator + CFG_FILE_NAME, true));
		final IPreferencesConfiguration pc = new PreferencesConfiguration(this);
		languageConfigAccessor = new LanguageConfigAccessor(pc);
	}

	private static ApplicationConfig getInstance() {
		if (instance == null) {
			synchronized (ApplicationConfig.class) {
				if (instance == null) { // The field needs to be volatile to prevent cache incoherence issues
					try {
						instance = new ApplicationConfig();
						if (++instanceCount > 1) {
							throw new IllegalStateException("Detected multiple instances of singleton " + instance.getClass());
						}
					}
					catch (final IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			}
		}
		return instance;
	}

	public static IPreferencesConfiguration getPreferencesConfiguration() {
		if (wrapper == null) {
			synchronized (ApplicationConfig.class) {
				if (wrapper == null) { // The field needs to be volatile to prevent cache incoherence issues
					wrapper = new PreferencesConfiguration(getInstance());
				}
			}
		}
		return wrapper;
	}

	public static void initialize() {
		try {
			final ApplicationConfig config = getInstance();
			GuiMessages.INSTANCE.setLanguage(config.languageConfigAccessor.getLanguage());
		}
		catch (final RuntimeException e) {
			throw new InitializationException(e);
		}
	}

}
