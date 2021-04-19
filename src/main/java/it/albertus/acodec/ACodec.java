package it.albertus.acodec;

import it.albertus.acodec.cli.CodecCli;
import it.albertus.acodec.gui.CodecGui;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ACodec {

	/* Unique entry point */
	public static void main(final String... args) {
		if (System.getProperty("C") != null || System.getProperty("c") != null) { // -DC
			CodecCli.main(args);
		}
		else if (System.getProperty("G") != null || System.getProperty("g") != null) { // -DG
			CodecGui.main(args);
		}
		else {
			if (args.length > 0) {
				CodecCli.main(args);
			}
			else {
				CodecGui.main(args);
			}
		}
	}

}
