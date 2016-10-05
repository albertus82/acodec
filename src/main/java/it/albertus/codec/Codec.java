package it.albertus.codec;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import it.albertus.codec.console.CodecConsole;
import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.gui.CodecGui;
import it.albertus.codec.resources.Messages;
import it.albertus.util.Version;

public class Codec {

	private final CodecEngine engine;

	protected Codec() {
		this.engine = new CodecEngine();
	}

	public static void main(final String[] args) {
		if (args.length > 0) {
			new CodecConsole().execute(args);
		}
		else {
			Display.setAppName(Messages.get("msg.application.name"));
			Display.setAppVersion(Version.getInstance().getNumber());
			final Display display = Display.getDefault();
			final Shell shell = new CodecGui(display).getShell();
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
