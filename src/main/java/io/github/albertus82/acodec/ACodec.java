package io.github.albertus82.acodec;

import io.github.albertus82.acodec.cli.CodecCli;
import io.github.albertus82.acodec.gui.CodecGui;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ACodec {

	public static void main(final String... args) {
		if (args.length > 0) {
			CodecCli.main(args);
		}
		else {
			CodecGui.main(args);
		}
	}

}
