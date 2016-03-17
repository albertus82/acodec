package it.albertus.codec.console;

import it.albertus.codec.Codec;
import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.resources.Resources;
import it.albertus.util.NewLine;
import it.albertus.util.Version;

public class CodecConsole extends Codec {

	private static final String HELP;

	static {
		StringBuilder help = new StringBuilder();
		help.append(Resources.get("msg.help.usage"));
		help.append(NewLine.SYSTEM_LINE_SEPARATOR).append(NewLine.SYSTEM_LINE_SEPARATOR);
		help.append(Resources.get("msg.help.modes")).append(NewLine.SYSTEM_LINE_SEPARATOR);
		for (final CodecMode mode : CodecMode.values()) {
			help.append("    ").append(mode.getAbbreviation()).append("    ").append(mode.getName()).append(NewLine.SYSTEM_LINE_SEPARATOR);
		}
		help.append(NewLine.SYSTEM_LINE_SEPARATOR);
		help.append(Resources.get("msg.help.algorithms")).append(NewLine.SYSTEM_LINE_SEPARATOR);
		for (final CodecAlgorithm algorithm : CodecAlgorithm.values()) {
			help.append("    ").append(algorithm.getName()).append(NewLine.SYSTEM_LINE_SEPARATOR);
		}
		help.append(NewLine.SYSTEM_LINE_SEPARATOR);
		help.append(Resources.get("msg.help.example"));
		HELP = help.toString();
	}

	/* java -jar codec.jar e|d base64|md2|md5|...|sha-512 "text to encode" */
	public void execute(String[] args) {
		CodecMode mode = null;
		CodecAlgorithm algorithm = null;

		if (args.length != 3) {
			System.out.println(Resources.get("msg.help.head", Version.getInstance().getNumber(), Version.getInstance().getDate()) + NewLine.SYSTEM_LINE_SEPARATOR + NewLine.SYSTEM_LINE_SEPARATOR + HELP);
			return;
		}

		/* Mode */
		final char modeArg = args[0].trim().toLowerCase().charAt(0);
		for (final CodecMode cm : CodecMode.values()) {
			if (cm.getAbbreviation() == modeArg) {
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

		getEngine().setAlgorithm(algorithm);
		getEngine().setMode(mode);

		/* Execution */
		try {
			System.out.println(getEngine().run(args[2]));
		}
		catch (Exception e) {
			System.err.println(Resources.get("err.generic", e.getMessage()) + NewLine.SYSTEM_LINE_SEPARATOR);
		}
	}

}
