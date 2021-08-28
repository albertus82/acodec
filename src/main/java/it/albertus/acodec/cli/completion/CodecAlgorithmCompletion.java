package it.albertus.acodec.cli.completion;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import it.albertus.acodec.common.engine.CodecAlgorithm;

public class CodecAlgorithmCompletion implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		return Arrays.stream(CodecAlgorithm.values()).map(a -> a.getName().toLowerCase(Locale.ROOT)).iterator();
	}

}
