package it.albertus.acodec.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

import it.albertus.acodec.ACodec;
import it.albertus.acodec.cli.completion.CharsetCompletion;
import it.albertus.acodec.cli.completion.CodecAlgorithmCompletion;
import it.albertus.acodec.cli.completion.CodecModeCompletion;
import it.albertus.acodec.cli.converter.CharsetConverter;
import it.albertus.acodec.cli.converter.CharsetConverter.InvalidCharsetException;
import it.albertus.acodec.cli.converter.CodecAlgorithmConverter;
import it.albertus.acodec.cli.converter.CodecAlgorithmConverter.InvalidAlgorithmException;
import it.albertus.acodec.cli.converter.CodecModeConverter;
import it.albertus.acodec.cli.converter.CodecModeConverter.InvalidModeException;
import it.albertus.acodec.cli.resources.ConsoleMessages;
import it.albertus.acodec.common.engine.CodecAlgorithm;
import it.albertus.acodec.common.engine.CodecConfig;
import it.albertus.acodec.common.engine.CodecMode;
import it.albertus.acodec.common.engine.ProcessFileTask;
import it.albertus.acodec.common.engine.StringCodec;
import it.albertus.acodec.common.resources.Messages;
import it.albertus.util.StringUtils;
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

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Command(versionProvider = VersionProvider.class)
@SuppressWarnings("java:S106") // Replace this use of System.out or System.err by a logger. Standard outputs should not be used directly to log anything (java:S106)
public class CodecCli implements Callable<Integer> {

	private static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();
	private static final short WIDTH = 80;

	private static final char OPTION_CHARSET = 'c';
	private static final char OPTION_FILE = 'f';
	private static final char OPTION_INTERACTIVE = 'i';
	private static final char OPTION_ERRORS = 'e';
	private static final char OPTION_HELP = 'h';
	private static final char OPTION_VERSION = 'v';

	private static final Messages messages = ConsoleMessages.INSTANCE;

	@Parameters(index = "0", completionCandidates = CodecModeCompletion.class)
	private CodecMode mode;

	@Parameters(index = "1", completionCandidates = CodecAlgorithmCompletion.class)
	private CodecAlgorithm algorithm;

	@Parameters(index = "2", arity = "0..1")
	private String inputTextCommandLine;

	@Option(names = { "-" + OPTION_INTERACTIVE, "--interactive" }, interactive = true, echo = false, description = "input")
	private String inputTextInteractive;

	@Option(names = { "-" + OPTION_CHARSET, "--charset" }, completionCandidates = CharsetCompletion.class)
	private Charset charset = Charset.defaultCharset();

	@Option(names = { "-" + OPTION_FILE, "--file" }, arity = "1..2", required = false)
	private File[] files;

	@Option(names = { "-" + OPTION_ERRORS, "--errors" })
	private boolean errors;

	@Option(names = { "-" + OPTION_HELP, "--help", "-?", "/?" }, help = true)
	private boolean usageHelpRequested;

	@Option(names = { "-" + OPTION_VERSION, "--version" }, versionHelp = true)
	private boolean versionInfoRequested;

	public static void main(final String... args) {
		System.exit(new CommandLine(new CodecCli()).setCommandName(ACodec.class.getSimpleName().toLowerCase(Locale.ROOT)).setOptionsCaseInsensitive(true).setParameterExceptionHandler((e, a) -> {
			if (e.getCause() instanceof InvalidCharsetException) {
				System.out.println(messages.get("console.error.invalid.charset", e.getCause().getMessage()));
			}
			else if (e.getCause() instanceof InvalidAlgorithmException) {
				System.out.println(messages.get("console.error.invalid.algorithm", e.getCause().getMessage()));
			}
			else if (e.getCause() instanceof InvalidModeException) {
				System.out.println(messages.get("console.error.invalid.mode", e.getCause().getMessage()));
			}
			else if (a.length != 0) {
				System.out.println(messages.get("console.error.incorrect.command.syntax"));
			}
			else {
				printHelp();
			}
			return ExitCode.USAGE;
		}).registerConverter(CodecMode.class, new CodecModeConverter()).registerConverter(CodecAlgorithm.class, new CodecAlgorithmConverter()).registerConverter(Charset.class, new CharsetConverter()).execute(args));
	}

	@Override
	public Integer call() {
		if (usageHelpRequested) {
			printHelp();
			return ExitCode.OK;
		}

		final String inputText = inputTextInteractive != null ? inputTextInteractive : inputTextCommandLine;

		if (files == null && inputText == null || files != null && inputText != null) {
			System.out.println(messages.get("console.error.incorrect.command.syntax"));
			return ExitCode.USAGE;
		}

		final CodecConfig config = new CodecConfig(mode, algorithm, charset);
		log.log(Level.CONFIG, "{0}", config);

		/* Execution */
		try {
			if (files != null && files.length > 0) {
				return processFile(config, files);
			}
			else {
				System.out.println(new StringCodec(config).run(inputText));
				return ExitCode.OK;
			}
		}
		catch (final EncoderException e) {
			printError(messages.get("console.error.cannot.encode", config.getAlgorithm().getName()), e);
			return ExitCode.SOFTWARE;
		}
		catch (final DecoderException e) {
			printError(messages.get("console.error.cannot.decode", config.getAlgorithm().getName()), e);
			return ExitCode.SOFTWARE;
		}
		catch (final Exception e) {
			printError(messages.get("console.error.unexpected.error"), e);
			return ExitCode.SOFTWARE;
		}
	}

	private void printError(final String message, final Exception e) {
		System.out.println(message);
		if (errors && e != null) {
			e.printStackTrace();
		}
	}

	private static int processFile(@NonNull final CodecConfig config, @NonNull final File[] files) throws EncoderException, DecoderException {
		if (!files[0].isFile()) {
			System.out.println(messages.get("console.message.missing.file", files[0]));
			return ExitCode.SOFTWARE;
		}
		if (files.length > 1 && files[1].isFile()) {
			System.out.print(messages.get("console.message.overwrite.file.question") + ' ');
			final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				final String userAnswer = StringUtils.trimToEmpty(br.readLine());
				final Collection<String> yesAnswers = Arrays.asList(messages.get("console.message.overwrite.file.answers.yes").split(","));
				if (!yesAnswers.contains(userAnswer.toLowerCase()) && !yesAnswers.contains(userAnswer.toLowerCase(Locale.ROOT))) {
					System.out.println(messages.get("console.message.file.process.cancel.message"));
					return ExitCode.OK; // exit immediately
				}
			}
			catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		final ProcessFileTask task;
		if (files.length > 1) {
			task = new ProcessFileTask(config, files[0], files[1]);
		}
		else {
			task = new ProcessFileTask(config, files[0]);
		}
		try {
			new ProcessFileRunnable(task, System.out).run();
		}
		catch (final FileNotFoundException e) {
			System.out.println(messages.get("console.message.missing.file", e.getMessage()));
			return ExitCode.SOFTWARE;
		}
		if (files.length > 1) {
			System.out.println(messages.get("console.message.file.process.ok.message"));
		}
		return ExitCode.OK;
	}

	private static void printHelp() {
		/* Usage */
		final StringBuilder help = new StringBuilder(messages.get("console.help.usage", OPTION_CHARSET, OPTION_FILE, OPTION_INTERACTIVE, OPTION_ERRORS));
		help.append(SYSTEM_LINE_SEPARATOR).append(SYSTEM_LINE_SEPARATOR);

		/* Modes */
		help.append(messages.get("console.help.modes")).append(SYSTEM_LINE_SEPARATOR);
		for (final CodecMode mode : CodecMode.values()) {
			help.append("    ").append(mode.getAbbreviation()).append("    ").append(messages.get("console.help.modes." + mode.getAbbreviation())).append(SYSTEM_LINE_SEPARATOR);
		}
		help.append(SYSTEM_LINE_SEPARATOR);

		/* Algorithms */
		help.append(buildHelpBlock(messages.get("console.help.algorithms"), Arrays.stream(CodecAlgorithm.values()).map(CodecAlgorithm::getName).collect(Collectors.toCollection(LinkedHashSet::new))));
		help.append(SYSTEM_LINE_SEPARATOR);

		/* Charsets */
		help.append(buildHelpBlock(messages.get("console.help.charsets"), Charset.availableCharsets().keySet()));
		help.append(' ').append(messages.get("console.help.default.charset", Charset.defaultCharset().name())).append(SYSTEM_LINE_SEPARATOR);
		help.append(SYSTEM_LINE_SEPARATOR);

		/* Example */
		help.append(messages.get("console.help.example"));

		/* Header */
		Date versionDate;
		try {
			versionDate = Version.getDate();
		}
		catch (final ParseException e) {
			log.log(Level.FINE, "Invalid version date:", e);
			versionDate = new Date();
		}
		System.out.println(messages.get("console.help.header", Version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM).format(versionDate)));
		System.out.println();
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
