package it.albertus.acodec.cli.completion;

import java.util.Arrays;
import java.util.Iterator;

import it.albertus.acodec.common.engine.CodecAlgorithm;

public class CodecAlgorithmCompletion implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		return Arrays.stream(CodecAlgorithm.values()).map(CodecAlgorithm::getName).iterator();
	}

}
