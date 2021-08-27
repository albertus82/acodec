package it.albertus.acodec.cli.completion;

import java.util.Arrays;
import java.util.Iterator;

import it.albertus.acodec.common.engine.CodecMode;

public class CodecModeCompletion implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		return Arrays.stream(CodecMode.values()).map(mode -> Character.toString(mode.getAbbreviation())).iterator();
	}

}
