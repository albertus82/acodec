package it.albertus.acodec.console;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import it.albertus.acodec.console.converter.CharsetConverter;
import it.albertus.acodec.console.converter.CodecAlgorithmConverter;
import it.albertus.acodec.console.converter.CodecModeConverter;
import it.albertus.acodec.console.converter.ConverterException;
import it.albertus.acodec.engine.CodecAlgorithm;
import it.albertus.acodec.engine.CodecConfig;
import it.albertus.acodec.engine.CodecMode;
import it.albertus.acodec.engine.ProcessFileTask;
import it.albertus.acodec.engine.StringCodec;
import it.albertus.acodec.resources.Messages;
import it.albertus.util.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.java.Log;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@SuppressWarnings("java:S106") // "Standard outputs should not be used directly to log anything"
@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Command
public class CodecConsole implements Runnable {

	private static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();
	private static final short WIDTH = 80;

	private static final char OPTION_CHARSET = 'c';
	private static final char OPTION_FILE = 'f';
	private static final char OPTION_HELP = 'h';

	@Parameters(index = "0")
	private CodecMode mode;

	@Parameters(index = "1")
	private CodecAlgorithm algorithm;

	@Parameters(index = "2", arity = "0..1")
	private String inputText;

	@Option(names = { "-" + OPTION_CHARSET, "--charset" })
	private Charset charset = Charset.defaultCharset();

	@Option(names = { "-" + OPTION_FILE, "--file" }, arity = "1..2", required = false)
	private File[] files;

	@Option(names = { "-" + OPTION_HELP, "--help" })
	private boolean helpRequested;

	public static void main(final String... args) {
		System.exit(new CommandLine(new CodecConsole()).setOptionsCaseInsensitive(true).setParameterExceptionHandler((e, a) -> {
			log.log(Level.FINE, e.toString(), e);
			if (e.getCause() instanceof ConverterException) {
				System.err.println(e.getCause().getMessage());
				System.out.println();
			}
			printHelp();
			return ExitCode.USAGE;
		}).registerConverter(CodecMode.class, new CodecModeConverter()).registerConverter(CodecAlgorithm.class, new CodecAlgorithmConverter()).registerConverter(Charset.class, new CharsetConverter()).execute(args));
	}

	@Override
	public void run() {
		if (helpRequested || files == null && inputText == null || files != null && inputText != null) {
			printHelp();
			return;
		}

		final CodecConfig config = new CodecConfig(mode, algorithm, charset);
		log.log(Level.FINE, "{0}", config);

		/* Execution */
		try {
			if (files != null && files.length > 0) {
				processFile(config, files);
			}
			else {
				System.out.println(new StringCodec(config).run(inputText));
			}
		}
		catch (final Exception e) {
			log.log(Level.FINE, e.toString(), e);
			final String message = e.getLocalizedMessage();
			System.err.println(message + (message.endsWith(".") ? "" : '.'));
		}
	}

	private static void processFile(@NonNull final CodecConfig config, @NonNull final File[] files) throws FileNotFoundException, InterruptedException {
		if (!files[0].isFile()) {
			throw new FileNotFoundException(Messages.get("msg.missing.file", files[0]));
		}
		final ProcessFileTask task = new ProcessFileTask(config, files[0], files.length > 1 ? files[1] : null);
		try {
			CompletableFuture.runAsync(new ProcessFileRunnable(task, System.out)).get();
			if (files.length > 1) {
				System.out.println(Messages.get("msg.file.process.ok.message"));
			}
		}
		catch (final ExecutionException e) {
			log.log(Level.FINE, e.toString(), e);
			final String message = e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getLocalizedMessage();
			System.err.println(message + (message.endsWith(".") ? "" : '.'));
		}
	}

	private static void printHelp() {
		/* Usage */
		final StringBuilder help = new StringBuilder(Messages.get("msg.help.usage", OPTION_CHARSET, OPTION_FILE));
		help.append(SYSTEM_LINE_SEPARATOR).append(SYSTEM_LINE_SEPARATOR);

		/* Modes */
		help.append(Messages.get("msg.help.modes")).append(SYSTEM_LINE_SEPARATOR);
		for (final CodecMode mode : CodecMode.values()) {
			help.append("    ").append(mode.getAbbreviation()).append("    ").append(mode.getName()).append(SYSTEM_LINE_SEPARATOR);
		}
		help.append(SYSTEM_LINE_SEPARATOR);

		/* Algorithms */
		help.append(buildHelpBlock(Messages.get("msg.help.algorithms"), Arrays.stream(CodecAlgorithm.values()).map(CodecAlgorithm::getName).collect(Collectors.toCollection(LinkedHashSet::new))));
		help.append(SYSTEM_LINE_SEPARATOR);

		/* Charsets */
		help.append(buildHelpBlock(Messages.get("msg.help.charsets"), Charset.availableCharsets().keySet()));
		help.append(' ').append(Messages.get("msg.help.default.charset", Charset.defaultCharset().name())).append(SYSTEM_LINE_SEPARATOR);
		help.append(SYSTEM_LINE_SEPARATOR);

		/* Example */
		help.append(Messages.get("msg.help.example"));

		final Version version = Version.getInstance();
		try {
			System.out.println(Messages.get("msg.application.name") + ' ' + Messages.get("msg.version", version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM).format(version.getDate())) + " [" + Messages.get("project.url") + ']');
			System.out.println();
		}
		catch (final RuntimeException e) {
			log.log(Level.FINE, e.toString(), e);
		}
		System.out.println(help.toString().trim());
	}

	private static CharSequence buildHelpBlock(@NonNull final String name, final @NonNull Iterable<String> values) {
		final StringBuilder block = new StringBuilder(name);
		block.append(' ');
		int cursorPosition = block.length();
		final int offset = cursorPosition;
		for (final String value : values) {
			final String toPrint = value + ", ";
			if (cursorPosition + toPrint.length() >= WIDTH) {
				block.append(SYSTEM_LINE_SEPARATOR);
				for (int i = 0; i < offset; i++) {
					block.append(' ');
				}
				cursorPosition = offset;
			}
			block.append(toPrint);
			cursorPosition += toPrint.length();
		}
		block.replace(block.length() - 2, block.length(), SYSTEM_LINE_SEPARATOR);
		return block;
	}

}
