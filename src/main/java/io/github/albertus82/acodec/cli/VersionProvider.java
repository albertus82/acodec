package io.github.albertus82.acodec.cli;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.logging.Level;

import io.github.albertus82.acodec.cli.resources.ConsoleMessages;
import io.github.albertus82.acodec.common.util.BuildInfo;
import lombok.extern.java.Log;
import picocli.CommandLine.IVersionProvider;

@Log
public class VersionProvider implements IVersionProvider {

	private static final ConsoleMessages messages = ConsoleMessages.INSTANCE;

	@Override
	public String[] getVersion() throws ParseException {
		return new String[] { messages.get("console.version", BuildInfo.getProperty("project.version"), DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(getVersionTimestamp())) };
	}

	private static TemporalAccessor getVersionTimestamp() {
		try {
			return DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(BuildInfo.getProperty("version.timestamp"));
		}
		catch (final RuntimeException e) {
			log.log(Level.FINE, "Invalid version timestamp, falling back to current datetime:", e);
			return LocalDateTime.now();
		}
	}

}
