package it.albertus.codec;

import it.albertus.codec.console.CodecConsole;
import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.gui.CodecGui;

public abstract class Codec {

	private final CodecEngine engine = new CodecEngine();

	/* Unique entry point */
	public static void main(final String[] args) {
		final String headerType = System.getProperty("launch4j.headerType");
		if (headerType != null) {
			if ("console".equalsIgnoreCase(headerType)) {
				CodecConsole.start(args);
			}
			else if ("gui".equalsIgnoreCase(headerType)) {
				CodecGui.start();
			}
		}
		else {
			if (args.length > 0) {
				CodecConsole.start(args);
			}
			else {
				CodecGui.start();
			}
		}
	}

	public CodecEngine getEngine() {
		return engine;
	}

}
