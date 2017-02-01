package it.albertus.codec;

import it.albertus.codec.console.CodecConsole;
import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.gui.CodecGui;

public abstract class Codec {

	private final CodecEngine engine = new CodecEngine();

	public static void main(final String[] args) {
		if (args.length > 0) {
			CodecConsole.start(args);
		}
		else {
			CodecGui.start();
		}
	}

	public CodecEngine getEngine() {
		return engine;
	}

}
