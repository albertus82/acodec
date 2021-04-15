package it.albertus.acodec;

import it.albertus.acodec.cli.CodecCli;
import it.albertus.acodec.gui.CodecGui;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ACodec {

	/* Unique entry point */
	public static void main(final String... args) {
		final String mode = System.getProperty(ACodec.class.getName() + ".main.mode");
		if (mode != null) {
			if ("cli".equalsIgnoreCase(mode)) {
				CodecCli.main(args);
			}
			else if ("gui".equalsIgnoreCase(mode)) {
				CodecGui.main();
			}
		}
		else {
			if (args.length > 0) {
				CodecCli.main(args);
			}
			else {
				CodecGui.main();
			}
		}
	}

}
