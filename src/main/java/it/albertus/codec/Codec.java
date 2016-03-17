package it.albertus.codec;

import it.albertus.codec.console.CodecConsole;
import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.gui.CodecGui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Codec {

	private final CodecEngine engine;

	public Codec() {
		this.engine = new CodecEngine();
	}

	public static void main(final String[] args) {
		if (args.length > 0) {
			new CodecConsole().execute(args);
		}
		else {
			final Display display = new Display();
			final Shell shell = new CodecGui().createShell(display);
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			display.dispose();
		}
	}

	public CodecEngine getEngine() {
		return engine;
	}

}
