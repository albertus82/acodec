package com.github.albertus82.acodec.cli.completion;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import com.github.albertus82.acodec.common.engine.CodecAlgorithm;

public class CodecAlgorithmCompletion implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		return Arrays.stream(CodecAlgorithm.values()).map(a -> a.getName().toLowerCase(Locale.ROOT)).iterator();
	}

}
