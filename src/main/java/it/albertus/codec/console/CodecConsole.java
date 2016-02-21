package it.albertus.codec.console;

import it.albertus.codec.Codec;
import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.resources.Resources;

public class CodecConsole extends Codec {

	/* java -jar codec.jar e|d base64|md2|md5|...|sha-512 "text to encode" */
	public void execute(String[] args) {
		CodecMode mode = null;
		CodecAlgorithm algorithm = null;

		if (args.length < 3) {
			System.out.println("Missing parameter"); // TODO help
			return;
		}

		/* Mode */
		final char modeArg = args[0].trim().toUpperCase().charAt(0);
		for (final CodecMode cm : CodecMode.values()) {
			if (cm.getAbbreviation() == modeArg) {
				mode = cm;
				break;
			}
		}
		if (mode == null) {
			System.err.println(Resources.get("err.invalid.mode", args[0].trim()));
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
			System.err.println(Resources.get("err.invalid.algorithm", algorithmArg));
			return;
		}

		engine.setAlgorithm(algorithm);
		engine.setMode(mode);

		/* Execution */
		try {
			System.out.println(engine.run(args[2]));
		}
		catch (Exception e) {
			System.err.println(Resources.get("err.generic", e.getMessage()));
		}
	}

}
