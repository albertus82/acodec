package it.albertus.codec.gui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import it.albertus.util.logging.LoggerFactory;

public class Images {

	private static final Logger logger = LoggerFactory.getLogger(Images.class);

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

	private static final Map<Rectangle, Image> mainIconMap;

	static {
		mainIconMap = loadImagesFromResource("main.ico");
	}

	private static Map<Rectangle, Image> loadImagesFromResource(final String resourceName) {
		final Map<Rectangle, Image> map = new TreeMap<>(areaComparatorDescending);
		try (final InputStream stream = Images.class.getResourceAsStream(resourceName)) {
			for (final ImageData data : new ImageLoader().load(stream)) {
				final Image image = new Image(Display.getCurrent(), data);
				map.put(image.getBounds(), image);
			}
			logger.log(Level.CONFIG, "{0}: {1}", new Object[] { resourceName, map });
			return map;
		}
		catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static Image[] getMainIconArray() {
		return getMainIconMap().values().toArray(new Image[0]);
	}

	/**
	 * Main application icon in various formats, sorted by size (area)
	 * <b>descending</b>.
	 */
	public static Map<Rectangle, Image> getMainIconMap() {
		return Collections.unmodifiableMap(mainIconMap);
	}

	private Images() {
		throw new IllegalAccessError();
	}

}
