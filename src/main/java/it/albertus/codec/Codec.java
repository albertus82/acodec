package it.albertus.codec;

import it.albertus.codec.console.CodecConsole;
import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.gui.CodecGui;

public abstract class Codec {

	private final CodecEngine engine = new CodecEngine();

	/* Unique entry point */
	public static void main(final String... args) {
		final String mode = System.getProperty(Codec.class.getName() + ".main.mode");
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
