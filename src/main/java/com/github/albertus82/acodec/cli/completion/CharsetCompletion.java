package com.github.albertus82.acodec.cli.completion;

import java.nio.charset.Charset;
import java.util.Iterator;

public class CharsetCompletion implements Iterable<String> {

	@Override
	public Iterator<String> iterator() {
		return Charset.availableCharsets().keySet().iterator();
	}

}
