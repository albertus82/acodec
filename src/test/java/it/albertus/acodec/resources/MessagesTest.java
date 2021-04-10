package it.albertus.acodec.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import it.albertus.acodec.ACodec;
import it.albertus.acodec.common.resources.CommonMessages;
import it.albertus.acodec.console.resources.ConsoleMessages;
import it.albertus.acodec.gui.resources.GuiMessages;
import it.albertus.util.StringUtils;
import lombok.NonNull;
import lombok.extern.java.Log;

@Log
public class MessagesTest {

	@Test
	public void checkProperties() throws IOException {
		Reflections reflections = new Reflections(ConsoleMessages.class.getPackage().getName(), new ResourcesScanner());
		Set<String> resourceNames = reflections.getResources(name -> name.contains(ConsoleMessages.class.getSimpleName().toLowerCase(Locale.ROOT)) && name.endsWith(".properties"));
		log.log(Level.INFO, "Resources found: {0}", resourceNames);
		checkProperties(resourceNames, "console.");

		reflections = new Reflections(GuiMessages.class.getPackage().getName(), new ResourcesScanner());
		resourceNames = reflections.getResources(name -> name.contains(GuiMessages.class.getSimpleName().toLowerCase(Locale.ROOT)) && name.endsWith(".properties"));
		log.log(Level.INFO, "Resources found: {0}", resourceNames);
		checkProperties(resourceNames, "gui.");

		reflections = new Reflections(CommonMessages.class.getPackage().getName(), new ResourcesScanner());
		resourceNames = reflections.getResources(name -> name.contains(CommonMessages.class.getSimpleName().toLowerCase(Locale.ROOT)) && name.endsWith(".properties"));
		log.log(Level.INFO, "Resources found: {0}", resourceNames);
		checkProperties(resourceNames, "common.");
	}

	private void checkProperties(@NonNull final Iterable<String> resourceNames, @NonNull final String prefix) throws IOException {
		final Collection<Properties> pp = new ArrayList<>();
		for (final String resourceName : resourceNames) {
			final Properties p = new Properties();
			pp.add(p);
			try (final InputStream is = getClass().getResourceAsStream('/' + resourceName)) {
				Assert.assertNotNull("Missing resource file: " + resourceName, is);
				p.load(is);
			}
			log.log(Level.INFO, "{0} messages found in: {1}", new Serializable[] { p.size(), resourceName });
			Assert.assertFalse("Empty resource file: " + resourceName, p.isEmpty());
		}
		pp.stream().reduce((p1, p2) -> {
			p1.keySet().forEach(e -> Assert.assertTrue("Invalid property key '" + e + "': expected prefix '" + prefix + "'!", e.toString().startsWith(prefix)));
			p2.keySet().forEach(e -> Assert.assertTrue("Invalid property key '" + e + "': expected prefix '" + prefix + "'!", e.toString().startsWith(prefix)));
			Assert.assertTrue("Uneven resource files!", p1.keySet().containsAll(p2.keySet()));
			Assert.assertTrue("Uneven resource files!", p2.keySet().containsAll(p1.keySet()));
			return p1;
		});
	}

	@Test
	public void checkMessages() throws IOException {
		final Properties testProperties = new Properties();
		try (final InputStream is = getClass().getResourceAsStream("/test.properties")) {
			testProperties.load(is);
		}
		final Path sourcesPath = Paths.get(testProperties.getProperty("project.build.sourceDirectory"), ACodec.class.getPackage().getName().replace('.', File.separatorChar));
		log.log(Level.INFO, "Sources path: {0}", sourcesPath);
		final Set<String> keys = new TreeSet<>();
		try (final Stream<Path> paths = Files.walk(sourcesPath).filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".java"))) {
			paths.forEach(path -> {
				log.log(Level.FINE, "{0}", path);
				try {
					// @formatter:off
					keys.addAll(Files.readAllLines(path).stream()
							.map(line -> line.trim().replace(" ", ""))
							.filter(e -> e.toLowerCase(Locale.ROOT).contains("messages.get(\""))
							.flatMap(e -> Arrays.stream(e.split("(?i)(?>=messages\\.get\\(\")|(?=messages\\.get\\(\")")))
							.filter(e -> e.toLowerCase(Locale.ROOT).startsWith("messages"))
							.map(e -> StringUtils.substringBefore(StringUtils.substringAfter(e, "\""), "\""))
							.filter(key -> !key.endsWith("."))
							.collect(Collectors.toSet()));
					// @formatter:on
				}
				catch (final IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		}
		Assert.assertFalse("No message keys found.", keys.isEmpty());
		log.log(Level.INFO, "Found {0} message keys referenced in sources.", keys.size());
		final Collection<String> consoleKeys = ConsoleMessages.INSTANCE.getKeys();
		final Collection<String> guiKeys = GuiMessages.INSTANCE.getKeys();
		for (final String key : new TreeSet<>(keys)) {
			if (key.startsWith("console.")) {
				Assert.assertTrue("Missing message key '" + key + "' in " + ConsoleMessages.class.getSimpleName() + '!', consoleKeys.contains(key));
			}
			else if (key.startsWith("gui.")) {
				Assert.assertTrue("Missing message key '" + key + "' in " + GuiMessages.class.getSimpleName() + '!', guiKeys.contains(key));
			}
			else if (key.startsWith("common.")) {
				Assert.assertTrue("Missing message key '" + key + "' in " + ConsoleMessages.class.getSimpleName() + '!', consoleKeys.contains(key));
				Assert.assertTrue("Missing message key '" + key + "' in " + GuiMessages.class.getSimpleName() + '!', guiKeys.contains(key));
			}
			else {
				Assert.assertTrue("Invalid message key prefix: '" + key + "'!", false);
			}
		}
	}

}
