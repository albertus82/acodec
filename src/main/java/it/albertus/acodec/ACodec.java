package it.albertus.acodec;

import it.albertus.acodec.console.CodecConsole;
import it.albertus.acodec.engine.CodecEngine;
import it.albertus.acodec.gui.CodecGui;

public abstract class ACodec {

	private final CodecEngine engine = new CodecEngine();

	/* Unique entry point */
	public static void main(final String... args) {
		final String mode = System.getProperty(ACodec.class.getName() + ".main.mode");
		if (mode != null) {
			if ("console".equalsIgnoreCase(mode)) {
				CodecConsole.main(args);
			}
			else if ("gui".equalsIgnoreCase(mode)) {
				CodecGui.main();
			}
		}
		else {
			if (args.length > 0) {
				CodecConsole.main(args);
			}
			else {
				CodecGui.main();
			}
		}
	}

	public CodecEngine getEngine() {
		return engine;
	}

}
