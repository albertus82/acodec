package it.albertus.acodec.console;

import java.io.File;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

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

@SuppressWarnings("java:S106") // "Standard outputs should not be used directly to log anything"
@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodecConsole {

	private static final String MSG_KEY_ERR_GENERIC = "err.generic";

	private static final String SYSTEM_LINE_SEPARATOR = System.lineSeparator();

	private static final char OPTION_CHARSET = 'c';
	private static final char OPTION_FILE = 'f';
	private static final String ARG_HELP = "--help";

	/* java -jar codec.jar e|d base64|md2|md5|...|sha-512 "text to encode" */
	public static void main(final String... args) {
		CodecMode mode = null;
		CodecAlgorithm algorithm = null;
		String charsetName = null;

		File inputFile = null;
		File outputFile = null;

		if (args.length < 3) {
			printHelp();
			return;
		}

		for (final String arg : args) {
			if (arg.equalsIgnoreCase(ARG_HELP)) {
				printHelp();
				return;
			}
		}

		/* Mode */
		final String modeArg = args[0].trim();
		for (final CodecMode cm : CodecMode.values()) {
			if (modeArg.equalsIgnoreCase(Character.toString(cm.getAbbreviation()))) {
				mode = cm;
				break;
			}
		}
		if (mode == null) {
			System.err.println(Messages.get("err.invalid.mode", args[0].trim()) + SYSTEM_LINE_SEPARATOR);
			printHelp();
			return;
		}

		/* Algorithm */
		final String algorithmArg = args[1].trim();
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getName().equalsIgnoreCase(algorithmArg) || ca.name().equalsIgnoreCase(algorithmArg) || ca.getAliases().stream().anyMatch(algorithmArg::equalsIgnoreCase)) {
				algorithm = ca;
				break;
			}
		}
		if (algorithm == null) {
			System.err.println(Messages.get("err.invalid.algorithm", algorithmArg) + SYSTEM_LINE_SEPARATOR);
			printHelp();
			return;
		}

		int expectedArgc = 3;

		/* Options */
		for (int i = 2; i < args.length; i++) {
			if (args[i].length() == 2 && args[i].charAt(0) == '-') {
				switch (Character.toLowerCase(args[i].charAt(1))) {
				case OPTION_CHARSET:
					if (args.length > i + 1 && i < args.length - 2) {
						charsetName = args[i + 1];
						expectedArgc += 2;
					}
					else {
						printHelp();
						return;
					}
					break;
				case OPTION_FILE:
					if (args.length > i + 2 && i < args.length - 2) {
						inputFile = new File(args[i + 1]).getAbsoluteFile();
						outputFile = new File(args[i + 2]).getAbsoluteFile();
						expectedArgc += 2;
					}
					else {
						printHelp();
						return;
					}
					break;
				default:
					System.err.println(Messages.get("err.invalid.option", args[i].charAt(1)) + SYSTEM_LINE_SEPARATOR);
					printHelp();
					return;
				}
			}
		}

		final CodecConfig config = new CodecConfig();
		config.setAlgorithm(algorithm);
		config.setMode(mode);
		if (charsetName != null) {
			try {
				config.setCharset(Charset.forName(charsetName));
			}
			catch (final Exception e) {
				log.log(Level.FINE, Messages.get("err.invalid.charset", charsetName), e);
				System.err.println(Messages.get("err.invalid.charset", charsetName) + SYSTEM_LINE_SEPARATOR);
				printHelp();
				return;
			}
		}

		/* Check arguments count */
		if (expectedArgc != args.length) {
			printHelp();
			return;
		}

		/* Execution */
		try {
			if (inputFile != null && outputFile != null) {
				final ProcessFileTask task = new ProcessFileTask(config, inputFile, outputFile);
				final Thread printProgressThread = newPrintProgressThread(task);
				printProgressThread.start();
				final String result = CompletableFuture.supplyAsync(() -> {
					try {
						return task.run(() -> false);
					}
					finally {
						printProgressThread.interrupt();
						try {
							printProgressThread.join();
						}
						catch (final InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
				}).get();
				System.out.println(Messages.get("msg.file.process.ok.message") + (result != null ? " -- " + result : ""));
			}
			else {
				System.out.println(new StringCodec(config).run(args[args.length - 1]));
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

	private static Thread newPrintProgressThread(final ProcessFileTask task) {
		final Thread printProgressThread = new Thread() {
			@Override
			public void run() {
				final long inputFileLength = task.getInputFile().length();
				final String part1 = Messages.get("msg.file.process.progress") + " (";
				System.out.print(part1);
				int charsToDelete = 0;
				while (task.getByteCount() < inputFileLength && !isInterrupted()) {
					final StringBuilder del = new StringBuilder();
					for (short i = 0; i < charsToDelete; i++) {
						del.append('\b');
					}
					final String part2 = (int) (task.getByteCount() / (double) inputFileLength * 100) + "%)";
					System.out.print(del + part2);
					charsToDelete = part2.length();
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					}
					catch (final InterruptedException e) {
						interrupt();
					}
				}
				final StringBuilder del = new StringBuilder();
				for (short i = 0; i < charsToDelete + part1.length(); i++) {
					del.append("\b \b");
				}
				System.out.print(del);
			}
		};
		printProgressThread.setDaemon(true); // This thread must not prevent the JVM from exiting.
		return printProgressThread;
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
			log.log(Level.WARNING, e.toString(), e);
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
