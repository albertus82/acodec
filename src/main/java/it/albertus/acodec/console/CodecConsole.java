package it.albertus.acodec.console;

import java.io.File;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import it.albertus.acodec.console.converter.CharsetConverter;
import it.albertus.acodec.console.converter.CodecAlgorithmConverter;
import it.albertus.acodec.console.converter.CodecModeConverter;
import it.albertus.acodec.engine.CodecAlgorithm;
import it.albertus.acodec.engine.CodecConfig;
import it.albertus.acodec.engine.CodecMode;
import it.albertus.acodec.engine.ProcessFileTask;
import it.albertus.acodec.engine.StringCodec;
import it.albertus.acodec.resources.Messages;
import it.albertus.util.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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

	private static final String MSG_KEY_ERR_GENERIC = "err.generic";

	private static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();

	private static final char OPTION_CHARSET = 'c';
	private static final char OPTION_FILE = 'f';

	@Parameters(index = "0")
	private CodecMode mode;

	@Parameters(index = "1")
	private CodecAlgorithm algorithm;

	@Parameters(index = "2", arity = "0..1")
	private String inputTextArg;

	@Option(names = { "-" + OPTION_CHARSET, "--charset" })
	private Charset charset = Charset.defaultCharset();

	@Option(names = { "-" + OPTION_FILE, "--file" }, arity = "1..2", required = false)
	private String[] fileArgs;

	@Option(names = { "-H", "--help" })
	private boolean helpArg;

	public static void main(final String... args) {
		System.exit(new CommandLine(new CodecConsole()).setOptionsCaseInsensitive(true).setParameterExceptionHandler((e, a) -> {
			if (e.getCause() instanceof IllegalArgumentException && e.getCause().getMessage() != null) {
				log.log(Level.FINE, e.getCause().getMessage(), e);
				System.err.println(e.getCause().getMessage());
			}
			printHelp();
			return ExitCode.USAGE;
		}).registerConverter(CodecMode.class, new CodecModeConverter()).registerConverter(CodecAlgorithm.class, new CodecAlgorithmConverter()).registerConverter(Charset.class, new CharsetConverter()).execute(args));
	}

	/* java -jar codec.jar e|d base64|md2|md5|...|sha-512 "text to encode" */
	@Override
	public void run() {
		File inputFile = null;
		File outputFile = null;

		if (helpArg) {
			printHelp();
			return;
		}

		if (fileArgs != null && fileArgs.length != 0) {
			inputFile = new File(fileArgs[0]).getAbsoluteFile();
			if (fileArgs.length > 1) {
				outputFile = new File(fileArgs[1]).getAbsoluteFile();
			}
		}
		else if (inputTextArg == null) {
			printHelp();
			return;
		}

		final CodecConfig config = new CodecConfig(mode, algorithm, charset);

		/* Execution */
		try {
			if (inputFile != null) {
				final ProcessFileTask task = new ProcessFileTask(config, inputFile, outputFile);
				CompletableFuture.runAsync(new ProcessFileRunnable(task)).get();
				if (outputFile != null) {
					System.out.println(Messages.get("msg.file.process.ok.message"));
				}
			}
			else {
				System.out.println(new StringCodec(config).run(inputTextArg));
			}
		}
		catch (final ExecutionException e) {
			log.log(Level.FINE, Messages.get(MSG_KEY_ERR_GENERIC, e.getCause() != null ? e.getCause().getMessage() : e.getMessage()), e);
			System.err.println(Messages.get(MSG_KEY_ERR_GENERIC, e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
		}
		catch (final Exception e) {
			log.log(Level.FINE, Messages.get(MSG_KEY_ERR_GENERIC, e.getMessage()), e);
			System.err.println(Messages.get(MSG_KEY_ERR_GENERIC, e.getMessage()));
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
		help.append(getAlgorithmsHelpBlock());
		help.append(SYSTEM_LINE_SEPARATOR);

		/* Charsets */
		help.append(getCharsetsHelpBlock());
		help.append(SYSTEM_LINE_SEPARATOR).append(SYSTEM_LINE_SEPARATOR);

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

	private static String getCharsetsHelpBlock() {
		final StringBuilder charsets = new StringBuilder(Messages.get("msg.help.charsets"));
		charsets.append(' ');
		int cursorPosition = charsets.length();
		final int offset = cursorPosition;
		for (final String charsetName : Charset.availableCharsets().keySet()) {
			final String toPrint = charsetName + ", ";
			if (cursorPosition + toPrint.length() >= 80) {
				charsets.append(SYSTEM_LINE_SEPARATOR);
				for (int i = 0; i < offset; i++) {
					charsets.append(' ');
				}
				cursorPosition = offset;
			}
			charsets.append(toPrint);
			cursorPosition += toPrint.length();
		}
		charsets.replace(charsets.length() - 2, charsets.length(), SYSTEM_LINE_SEPARATOR);
		charsets.append(' ').append(Messages.get("msg.help.default.charset", Charset.defaultCharset().name()));
		return charsets.toString();
	}

	private static String getAlgorithmsHelpBlock() {
		final StringBuilder algorithms = new StringBuilder(Messages.get("msg.help.algorithms"));
		algorithms.append(' ');
		int cursorPosition = algorithms.length();
		final int offset = cursorPosition;
		for (final CodecAlgorithm algorithm : CodecAlgorithm.values()) {
			final String toPrint = algorithm.getName() + ", ";
			if (cursorPosition + toPrint.length() >= 80) {
				algorithms.append(SYSTEM_LINE_SEPARATOR);
				for (int i = 0; i < offset; i++) {
					algorithms.append(' ');
				}
				cursorPosition = offset;
			}
			algorithms.append(toPrint);
			cursorPosition += toPrint.length();
		}
		algorithms.replace(algorithms.length() - 2, algorithms.length(), SYSTEM_LINE_SEPARATOR);
		return algorithms.toString();
	}

}
