package io.github.albertus82.acodec.gui.listener;

import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.albertus82.acodec.common.engine.CodecAlgorithm;

class ProcessFileActionTest {

	@Test
	void testBuildFilterExtensions() {
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			final String[] filter = ProcessFileAction.buildFilterExtensions(ca);
			Assertions.assertTrue(Arrays.stream(filter).anyMatch(x -> x.contains(ca.getFileExtension().toLowerCase(Locale.ROOT))), Arrays.toString(filter));
			Assertions.assertTrue(Arrays.stream(filter).anyMatch(x -> x.contains(ca.getFileExtension().toUpperCase(Locale.ROOT))), Arrays.toString(filter));
			Assertions.assertEquals(1, Arrays.stream(filter).filter(x -> x.contains("*.*")).count(), Arrays.toString(filter));
			Assertions.assertEquals(2, filter.length, Arrays.toString(filter));
		}
	}

}
