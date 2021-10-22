package it.albertus.acodec.gui;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.swing.SortOrder;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import it.albertus.jface.ImageUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Images {

	/**
	 * Main application icon in various formats, sorted by size (area)
	 * <b>descending</b>.
	 */
	@Getter
	private static final Map<Rectangle, Image> appIconMap = Collections.unmodifiableMap(loadFromResources(Images.class.getPackage().getName() + ".icon.app"));

	private static Map<Rectangle, Image> loadFromResources(final String packageName) {
		final Reflections reflections = new Reflections(packageName, Scanners.Resources.filterResultsBy(name -> name.toLowerCase(Locale.ROOT).endsWith(".png")));
		final Iterable<String> resourceNames = reflections.getResources(".*").stream().map(name -> '/' + name).collect(Collectors.toSet());
		final Map<Rectangle, Image> map = ImageUtils.createImageMap(resourceNames, SortOrder.DESCENDING);
		log.log(Level.CONFIG, "{0}: {1}", new Object[] { packageName, map });
		return map;
	}

	public static Image[] getAppIconArray() {
		return appIconMap.values().toArray(new Image[0]);
	}

}
