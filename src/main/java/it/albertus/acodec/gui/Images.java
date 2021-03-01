package it.albertus.acodec.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Images {

	private static final Comparator<Rectangle> areaComparatorDescending = (r1, r2) -> {
		final int a1 = r1.width * r1.height;
		final int a2 = r2.width * r2.height;
		if (a1 > a2) {
			return -1;
		}
		if (a1 < a2) {
			return 1;
		}
		return 0;
	};

	/**
	 * Main application icon in various formats, sorted by size (area)
	 * <b>descending</b>.
	 */
	@Getter
	private static final Map<Rectangle, Image> appIconMap = Collections.unmodifiableMap(loadFromResource(Images.class.getPackage().getName() + ".icon.app"));

	private static Map<Rectangle, Image> loadFromResource(final String resourceName) {
		final Reflections reflections = new Reflections(resourceName, new ResourcesScanner());
		final Iterable<String> fileNames = reflections.getResources(Pattern.compile(".*\\.png"));

		final Map<Rectangle, Image> map = new TreeMap<>(areaComparatorDescending);
		for (final String fileName : fileNames) {
			try (final InputStream stream = Images.class.getResourceAsStream('/' + fileName)) {
				for (final ImageData data : new ImageLoader().load(stream)) {
					final Image image = new Image(Display.getCurrent(), data);
					map.put(image.getBounds(), image);
				}
			}
			catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		log.log(Level.CONFIG, "{0}: {1}", new Object[] { resourceName, map });
		return map;
	}

	public static Image[] getAppIconArray() {
		return appIconMap.values().toArray(new Image[0]);
	}

}
