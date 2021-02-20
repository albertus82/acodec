package it.albertus.acodec.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Level;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import it.albertus.acodec.gui.listener.LinkSelectionListener;
import it.albertus.acodec.gui.resources.GuiMessages;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.closeable.CloseableResource;
import it.albertus.util.Version;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
public class AboutDialog extends Dialog {

	private static final double MONITOR_SIZE_DIVISOR = 1.2;

	private static final String SYM_NAME_FONT_DEFAULT = AboutDialog.class.getName() + ".default";

	private static final int COL_IDX_THIRDPARTY_AUTHOR = 0;
	private static final int COL_IDX_THIRDPARTY_LICENSE = 1;
	private static final int COL_IDX_THIRDPARTY_HOMEPAGE = 2;

	public AboutDialog(final Shell parent) {
		this(parent, SWT.SHEET);
	}

	public AboutDialog(final Shell parent, final int style) {
		super(parent, style);
		this.setText(GuiMessages.get("gui.label.about.title"));
	}

	public void open() {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.setImage(Images.getMainIconMap().get(new Rectangle(0, 0, 16, 16)));
		createContents(shell);
		constrainShellSize(shell);
		shell.open();
	}

	private static void createContents(final Shell shell) {
		GridLayoutFactory.swtDefaults().applyTo(shell);

		final LinkSelectionListener linkSelectionListener = new LinkSelectionListener();

		final Link info = new Link(shell, SWT.WRAP);
		final FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		if (!fontRegistry.hasValueFor(SYM_NAME_FONT_DEFAULT)) {
			fontRegistry.put(SYM_NAME_FONT_DEFAULT, info.getFont().getFontData());
		}
		info.setFont(fontRegistry.getBold(SYM_NAME_FONT_DEFAULT));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(info);
		Date versionDate;
		try {
			versionDate = Version.getDate();
		}
		catch (final ParseException e) {
			log.log(Level.WARNING, "Invalid version date:", e);
			versionDate = new Date();
		}
		info.setText(buildAnchor(GuiMessages.get("gui.message.project.url"), GuiMessages.get("gui.message.application.name")) + ' ' + GuiMessages.get("gui.message.version", Version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM, GuiMessages.getLanguage().getLocale()).format(versionDate)));
		info.addSelectionListener(linkSelectionListener);

		final Link acknowledgementsLocations = new Link(shell, SWT.WRAP);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(acknowledgementsLocations);
		acknowledgementsLocations.setText(GuiMessages.get("gui.label.about.acknowledgements.icon", buildAnchor(GuiMessages.get("gui.message.icon.url"), GuiMessages.get("gui.label.icon"))));
		acknowledgementsLocations.addSelectionListener(linkSelectionListener);

		final Link linkLicense = new Link(shell, SWT.WRAP);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(linkLicense);
		linkLicense.setText(GuiMessages.get("gui.label.about.license", buildAnchor(GuiMessages.get("gui.message.gpl.url"), GuiMessages.get("gui.label.gpl"))));
		linkLicense.addSelectionListener(linkSelectionListener);

		final Text appLicense = new Text(shell, SWT.BORDER | SWT.V_SCROLL);
		appLicense.setText(loadTextResource("/META-INF/LICENSE.txt"));
		appLicense.setEditable(false);
		appLicense.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).hint(SWT.DEFAULT, SwtUtils.convertVerticalDLUsToPixels(appLicense, 80)).applyTo(appLicense);

		final Label thirdPartySoftwareLabel = new Label(shell, SWT.WRAP);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(thirdPartySoftwareLabel);
		thirdPartySoftwareLabel.setText(GuiMessages.get("gui.label.about.3rdparty"));
		createThirdPartySoftwareTable(shell);

		final Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText(GuiMessages.get("gui.label.button.ok"));
		final int buttonWidth = SwtUtils.convertHorizontalDLUsToPixels(okButton, IDialogConstants.BUTTON_WIDTH);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).minSize(buttonWidth, SWT.DEFAULT).applyTo(okButton);
		okButton.setFocus();
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent se) {
				shell.close();
			}
		});
		shell.setDefaultButton(okButton);
	}

	private static void constrainShellSize(final Shell shell) {
		final int preferredWidth = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
		final int clientWidth = shell.getMonitor().getClientArea().width;
		if (preferredWidth > clientWidth / MONITOR_SIZE_DIVISOR) {
			final int wHint = (int) (clientWidth / MONITOR_SIZE_DIVISOR);
			shell.setSize(wHint, shell.computeSize(wHint, SWT.DEFAULT, true).y);
		}
		else {
			shell.pack();
		}
		shell.setMinimumSize(shell.getSize());
	}

	private static String buildAnchor(final String href, final String label) {
		return new StringBuilder("<a href=\"").append(href).append("\">").append(label).append("</a>").toString();
	}

	private static String loadTextResource(final String name) {
		final StringBuilder text = new StringBuilder();
		try (final InputStream is = AboutDialog.class.getResourceAsStream(name); final InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8); final BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				text.append(System.lineSeparator()).append(line);
			}
		}
		catch (final Exception e) {
			log.log(Level.WARNING, e.toString(), e);
		}
		return text.length() <= System.lineSeparator().length() ? "" : text.substring(System.lineSeparator().length());
	}

	private static void createThirdPartySoftwareTable(final Composite parent) {
		final TableViewer tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(tableViewer);
		final Table table = tableViewer.getTable();
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(false);

		final TableViewerColumn authorColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		authorColumn.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(final ViewerCell cell) {
				if (cell.getElement() instanceof ThirdPartySoftware) {
					final ThirdPartySoftware element = (ThirdPartySoftware) cell.getElement();
					cell.setText(element.getAuthor());
				}
			}
		});

		final TableViewerColumn licenseColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		licenseColumn.setLabelProvider(new StyledCellLabelProvider() { // NOSONAR Cannot avoid extending this JFace class.
			@Override
			public void update(final ViewerCell cell) {
				setLinkStyle(cell, GuiMessages.get("gui.label.about.3rdparty.license"));
				super.update(cell);
			}

			@Override
			public String getToolTipText(final Object o) {
				if (o instanceof ThirdPartySoftware) {
					final ThirdPartySoftware element = (ThirdPartySoftware) o;
					return element.getLicenseUri().toString();
				}
				else {
					return super.getToolTipText(o);
				}
			}
		});

		final TableViewerColumn homePageColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		homePageColumn.setLabelProvider(new StyledCellLabelProvider() { // NOSONAR Cannot avoid extending this JFace class.
			@Override
			public void update(final ViewerCell cell) {
				setLinkStyle(cell, GuiMessages.get("gui.label.about.3rdparty.homepage"));
				super.update(cell);
			}

			@Override
			public String getToolTipText(final Object o) {
				if (o instanceof ThirdPartySoftware) {
					final ThirdPartySoftware element = (ThirdPartySoftware) o;
					return element.getHomePageUri().toString();
				}
				else {
					return super.getToolTipText(o);
				}
			}
		});

		tableViewer.add(ThirdPartySoftware.loadFromProperties().toArray());

		packColumns(table);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e) {
				if (e.button == 1) {
					final ViewerCell cell = tableViewer.getCell(new Point(e.x, e.y));
					if (cell != null && cell.getElement() instanceof ThirdPartySoftware) {
						final ThirdPartySoftware element = (ThirdPartySoftware) cell.getElement();
						if (cell.getColumnIndex() == COL_IDX_THIRDPARTY_LICENSE) {
							Program.launch(element.getLicenseUri().toString());
						}
						else if (cell.getColumnIndex() == COL_IDX_THIRDPARTY_HOMEPAGE) {
							Program.launch(element.getHomePageUri().toString());
						}
					}
				}
			}
		});

		table.addMouseMoveListener(e -> {
			final ViewerCell cell = tableViewer.getCell(new Point(e.x, e.y));
			if (cell != null && cell.getColumnIndex() != COL_IDX_THIRDPARTY_AUTHOR) {
				if (parent.getCursor() == null) {
					parent.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
				}
			}
			else if (parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND).equals(parent.getCursor())) {
				parent.setCursor(null);
			}
		});
	}

	private static void setLinkStyle(final ViewerCell cell, final String label) {
		cell.setForeground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND));
		cell.setText(label);
		final StyleRange styleRange = new StyleRange();
		styleRange.underline = true;
		styleRange.length = label.length();
		cell.setStyleRanges(new StyleRange[] { styleRange });
	}

	private static void packColumns(final Table table) {
		for (final TableColumn column : table.getColumns()) {
			packColumn(column);
		}
	}

	private static void packColumn(final TableColumn column) {
		column.pack();
		if (Util.isGtk()) { // colmuns are badly resized on GTK, more space is actually needed
			try (final CloseableResource<GC> cr = new CloseableResource<>(new GC(column.getParent()))) {
				column.setWidth(column.getWidth() + cr.getResource().stringExtent("  ").x);
			}
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@Getter(AccessLevel.PRIVATE)
	@EqualsAndHashCode(onlyExplicitlyIncluded = true)
	private static class ThirdPartySoftware implements Comparable<ThirdPartySoftware> {

		@EqualsAndHashCode.Include
		private final String author;
		private final URI licenseUri;
		private final URI homePageUri;

		@Override
		public int compareTo(final ThirdPartySoftware o) {
			return author.compareTo(o.author);
		}

		private static Collection<ThirdPartySoftware> loadFromProperties() {
			final Properties properties = new Properties();
			try (final InputStream is = ThirdPartySoftware.class.getResourceAsStream("3rdparty.properties")) {
				properties.load(is);
			}
			catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
			final Collection<ThirdPartySoftware> set = new TreeSet<>();
			for (byte i = 1; i < Byte.MAX_VALUE; i++) {
				final String author = properties.getProperty(i + ".author");
				if (author == null) {
					break;
				}
				set.add(new ThirdPartySoftware(author, URI.create(properties.getProperty(i + ".licenseUri")), URI.create(properties.getProperty(i + ".homePageUri"))));
			}
			return set;
		}
	}
}
