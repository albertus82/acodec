package it.albertus.codec.console;

import it.albertus.codec.Codec;
import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.resources.Resources;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

import java.nio.charset.Charset;

public class CodecConsole extends Codec {

	private static final String ARG_CHARSET = "-c";
	private static final String ARG_HELP = "--help";

	private static final String HELP;

	static {
		/* Usage */
		final StringBuilder help = new StringBuilder(Resources.get("msg.help.usage"));
		help.append(NewLine.SYSTEM_LINE_SEPARATOR).append(NewLine.SYSTEM_LINE_SEPARATOR);

		/* Modes */
		help.append(Resources.get("msg.help.modes")).append(NewLine.SYSTEM_LINE_SEPARATOR);
		for (final CodecMode mode : CodecMode.values()) {
			help.append("    ").append(mode.getAbbreviation()).append("    ").append(mode.getName()).append(NewLine.SYSTEM_LINE_SEPARATOR);
		}
		help.append(NewLine.SYSTEM_LINE_SEPARATOR);

		/* Algorithms */
		help.append(Resources.get("msg.help.algorithms")).append(NewLine.SYSTEM_LINE_SEPARATOR);
		for (final CodecAlgorithm algorithm : CodecAlgorithm.values()) {
			help.append("    ").append(algorithm.getName()).append(NewLine.SYSTEM_LINE_SEPARATOR);
		}
		help.append(NewLine.SYSTEM_LINE_SEPARATOR);

		/* Charsets */
		final StringBuilder charsets = new StringBuilder(Resources.get("msg.help.charsets"));
		charsets.append(' ');
		int pos = charsets.length();
		final int offset = pos;
		for (final String charsetName : Charset.availableCharsets().keySet()) {
			final String toPrint = charsetName + ", ";
			if (pos + toPrint.length() >= 80) {
				charsets.append(NewLine.SYSTEM_LINE_SEPARATOR);
				for (int i = 0; i < offset; i++) {
					charsets.append(' ');
				}
				pos = offset;
			}
			charsets.append(toPrint);
			pos += toPrint.length();
		}
		help.append(charsets.replace(charsets.length() - 2, charsets.length(), NewLine.SYSTEM_LINE_SEPARATOR));
		help.append(NewLine.SYSTEM_LINE_SEPARATOR);

		/* Example */
		help.append(Resources.get("msg.help.example"));
		help.append(NewLine.SYSTEM_LINE_SEPARATOR);
		HELP = help.toString();
	}

	/* java -jar codec.jar e|d base64|md2|md5|...|sha-512 "text to encode" */
	public void execute(String[] args) {
		CodecMode mode = null;
		CodecAlgorithm algorithm = null;
		String charsetName = null;

		final String stringToProcess;

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
			System.err.println(Resources.get("err.invalid.mode", args[0].trim()) + NewLine.SYSTEM_LINE_SEPARATOR);
			System.out.println(HELP);
			return;
		}

		/* Algorithm */
		final String algorithmArg = args[1].trim();
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getName().equalsIgnoreCase(algorithmArg)) {
				algorithm = ca;
				break;
			}
		}
		if (algorithm == null) {
			System.err.println(Resources.get("err.invalid.algorithm", algorithmArg) + NewLine.SYSTEM_LINE_SEPARATOR);
			System.out.println(HELP);
			return;
		}

		/* Charset */
		if (ARG_CHARSET.equalsIgnoreCase(args[2].trim())) {
			if (args.length != 5) {
				printHelp();
				return;
			}
			stringToProcess = args[4];
			charsetName = args[3].trim();
		}
		else {
			if (args.length != 3) {
				printHelp();
				return;
			}
			stringToProcess = args[2];
		}

		getEngine().setAlgorithm(algorithm);
		getEngine().setMode(mode);
		if (charsetName != null) {
			try {
				getEngine().setCharset(Charset.forName(charsetName));
			}
			catch (Exception e) {
				System.err.println(Resources.get("err.invalid.charset", charsetName) + NewLine.SYSTEM_LINE_SEPARATOR);
				System.out.println(HELP);
				return;
			}
		}

		/* Execution */
		try {
			System.out.println(getEngine().run(stringToProcess));
			System.out.println();
		}
		catch (Exception e) {
			System.err.println(Resources.get("err.generic", e.getMessage()) + NewLine.SYSTEM_LINE_SEPARATOR);
		}
	}

	private void printHelp() {
		System.out.println(Resources.get("msg.application.name") + ' ' + Resources.get("msg.version", Version.getInstance().getNumber(), Version.getInstance().getDate()) + " [" + Resources.get("msg.website") + ']' + NewLine.SYSTEM_LINE_SEPARATOR + NewLine.SYSTEM_LINE_SEPARATOR + HELP);
	}

}
