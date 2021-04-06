package it.albertus.acodec.gui.listener;

import java.util.Arrays;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import it.albertus.acodec.common.engine.CodecAlgorithm;

public class ProcessFileActionTest {

	@Test
	public void testBuildFilterExtensions() {
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			final String[] filter = ProcessFileAction.buildFilterExtensions(ca);
			Assert.assertTrue(Arrays.toString(filter), Arrays.stream(filter).anyMatch(x -> x.contains(ca.getFileExtension().toLowerCase(Locale.ROOT)))); // NOSONAR
			Assert.assertTrue(Arrays.toString(filter), Arrays.stream(filter).anyMatch(x -> x.contains(ca.getFileExtension().toUpperCase(Locale.ROOT)))); // NOSONAR
			Assert.assertEquals(Arrays.toString(filter), 1, Arrays.stream(filter).filter(x -> x.contains("*.*")).count()); // NOSONAR
			Assert.assertEquals(Arrays.toString(filter), 2, filter.length);
		}
	}

}
