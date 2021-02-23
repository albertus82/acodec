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
import java.util.Optional;
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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import it.albertus.acodec.common.resources.ConfigurableMessages;
import it.albertus.acodec.gui.listener.LinkSelectionListener;
import it.albertus.acodec.gui.resources.GuiMessages;
import it.albertus.jface.SwtUtils;
import it.albertus.jface.closeable.CloseableResource;
import it.albertus.util.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
public class AboutDialog extends Dialog {

	private static final double MONITOR_SIZE_DIVISOR = 1.2;

	private static final int SCROLLABLE_VERTICAL_SIZE_DLUS = 25;

	private static final String SYM_NAME_FONT_DEFAULT = AboutDialog.class.getName() + ".default";

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	public AboutDialog(final Shell parent) {
		this(parent, SWT.SHEET | SWT.RESIZE);
	}

	public AboutDialog(final Shell parent, final int style) {
		super(parent, style);
		this.setText(messages.get("gui.label.about.title"));
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
		info.setText(buildAnchor(messages.get("gui.message.project.url"), messages.get("gui.message.application.name")) + ' ' + messages.get("gui.message.version", Version.getNumber(), DateFormat.getDateInstance(DateFormat.MEDIUM, messages.getLanguage().getLocale()).format(versionDate)));
		info.addSelectionListener(linkSelectionListener);

		final Link acknowledgementsLocations = new Link(shell, SWT.WRAP);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(acknowledgementsLocations);
		acknowledgementsLocations.setText(messages.get("gui.label.about.acknowledgements.icon", buildAnchor(messages.get("gui.message.icon.url"), messages.get("gui.label.icon"))));
		acknowledgementsLocations.addSelectionListener(linkSelectionListener);

		final Link linkLicense = new Link(shell, SWT.WRAP);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(linkLicense);
		linkLicense.setText(messages.get("gui.label.about.license", buildAnchor(messages.get("gui.message.gpl.url"), messages.get("gui.label.gpl"))));
		linkLicense.addSelectionListener(linkSelectionListener);

		final Text appLicense = new Text(shell, SWT.BORDER | SWT.V_SCROLL);
		appLicense.setText(loadTextResource("/META-INF/LICENSE.txt"));
		appLicense.setEditable(false);
		appLicense.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, SwtUtils.convertVerticalDLUsToPixels(appLicense, SCROLLABLE_VERTICAL_SIZE_DLUS)).applyTo(appLicense);

		final Label thirdPartySoftwareLabel = new Label(shell, SWT.WRAP);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(thirdPartySoftwareLabel);
		thirdPartySoftwareLabel.setText(messages.get("gui.label.about.3rdparty"));

		final ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledComposite.setLayout(new FillLayout());
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		final ThirdPartySoftwareTable thirdPartySoftwareTable = new ThirdPartySoftwareTable(scrolledComposite, Optional.empty());
		scrolledComposite.setContent(thirdPartySoftwareTable.getTableViewer().getControl());
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, SwtUtils.convertVerticalDLUsToPixels(scrolledComposite, SCROLLABLE_VERTICAL_SIZE_DLUS)).applyTo(scrolledComposite);

		final Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText(messages.get("gui.label.button.ok"));
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
		final Point preferredSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		final int clientWidth = shell.getMonitor().getClientArea().width;
		final int desiredWidth;
		if (preferredSize.x > clientWidth / MONITOR_SIZE_DIVISOR) {
			desiredWidth = (int) (clientWidth / MONITOR_SIZE_DIVISOR);
		}
		else {
			desiredWidth = preferredSize.x;
		}
		shell.setSize(desiredWidth, shell.getSize().y);
		shell.setMinimumSize(desiredWidth, preferredSize.y);
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

	@Getter
	private static class ThirdPartySoftwareTable {

		private static final byte COL_IDX_THIRDPARTY_NAME = 0;
		private static final byte COL_IDX_THIRDPARTY_AUTHOR = 1;
		private static final byte COL_IDX_THIRDPARTY_LICENSE = 2;
		private static final byte COL_IDX_THIRDPARTY_HOMEPAGE = 3;

		private final TableViewer tableViewer;

		private ThirdPartySoftwareTable(@NonNull final Composite parent, @NonNull final Optional<Object> layoutData) {
			tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
			ColumnViewerToolTipSupport.enableFor(tableViewer);
			final Table table = tableViewer.getTable();
			if (layoutData.isPresent()) {
				table.setLayoutData(layoutData.get());
			}
			table.setLinesVisible(true);
			table.setHeaderVisible(true);

			createNameColumn();
			createAuthorColumn();
			createLicenseColumn();
			createHomePageColumn();

			tableViewer.add(ThirdPartySoftware.loadFromProperties().toArray());

			packColumns();

			configureMouseListener();
			configureMouseMoveListener();
		}

		private void createNameColumn() {
			final TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.getColumn().setText(messages.get("gui.label.about.3rdparty.name"));
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					if (cell.getElement() instanceof ThirdPartySoftware) {
						final ThirdPartySoftware element = (ThirdPartySoftware) cell.getElement();
						cell.setText(element.getName());
					}
				}
			});
		}

		private void createAuthorColumn() {
			final TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.getColumn().setText(messages.get("gui.label.about.3rdparty.author"));
			column.setLabelProvider(new CellLabelProvider() {
				@Override
				public void update(final ViewerCell cell) {
					if (cell.getElement() instanceof ThirdPartySoftware) {
						final ThirdPartySoftware element = (ThirdPartySoftware) cell.getElement();
						cell.setText(element.getAuthor());
					}
				}
			});
		}

		private void createLicenseColumn() {
			final TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.setLabelProvider(new StyledCellLabelProvider() { // NOSONAR Cannot avoid extending this JFace class.
				@Override
				public void update(final ViewerCell cell) {
					setLinkStyle(cell, messages.get("gui.label.about.3rdparty.license"));
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
		}

		private void createHomePageColumn() {
			final TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
			column.setLabelProvider(new StyledCellLabelProvider() { // NOSONAR Cannot avoid extending this JFace class.
				@Override
				public void update(final ViewerCell cell) {
					setLinkStyle(cell, messages.get("gui.label.about.3rdparty.homepage"));
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
		}

		private void configureMouseListener() {
			tableViewer.getTable().addMouseListener(new MouseAdapter() {
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
		}

		private void configureMouseMoveListener() {
			final Table table = tableViewer.getTable();
			final Control parent = table.getParent();
			table.addMouseMoveListener(e -> {
				final ViewerCell cell = tableViewer.getCell(new Point(e.x, e.y));
				if (cell != null && cell.getColumnIndex() != COL_IDX_THIRDPARTY_NAME && cell.getColumnIndex() != COL_IDX_THIRDPARTY_AUTHOR) {
					if (parent.getCursor() == null) {
						parent.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
					}
				}
				else if (parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND).equals(parent.getCursor())) {
					parent.setCursor(null);
				}
			});
		}

		private void packColumns() {
			for (final TableColumn column : tableViewer.getTable().getColumns()) {
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

		private static void setLinkStyle(final ViewerCell cell, final String label) {
			cell.setForeground(cell.getControl().getDisplay().getSystemColor(SWT.COLOR_LINK_FOREGROUND));
			cell.setText(label);
			final StyleRange styleRange = new StyleRange();
			styleRange.underline = true;
			styleRange.length = label.length();
			cell.setStyleRanges(new StyleRange[] { styleRange });
		}

		@Getter(AccessLevel.PRIVATE)
		@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
		private static class ThirdPartySoftware implements Comparable<ThirdPartySoftware> {

			private final String name;
			private final String author;
			private final URI licenseUri;
			private final URI homePageUri;

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
					final String name = properties.getProperty(i + ".name");
					if (name == null) {
						break;
					}
					set.add(new ThirdPartySoftware(name, properties.getProperty(i + ".author"), URI.create(properties.getProperty(i + ".licenseUri")), URI.create(properties.getProperty(i + ".homePageUri"))));
				}
				return set;
			}

			@Override
			public boolean equals(final Object obj) {
				if (this == obj) {
					return true;
				}
				if (!(obj instanceof ThirdPartySoftware)) {
					return false;
				}
				final ThirdPartySoftware other = (ThirdPartySoftware) obj;
				return name.equalsIgnoreCase(other.name);
			}

			@Override
			public int hashCode() {
				return name.toLowerCase().hashCode();
			}

			@Override
			public int compareTo(final ThirdPartySoftware o) {
				return name.compareToIgnoreCase(o.name);
			}
		}
	}

}
