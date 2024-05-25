package io.github.albertus82.acodec.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.naming.SizeLimitExceededException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import io.github.albertus82.acodec.common.engine.CodecAlgorithm;
import io.github.albertus82.acodec.common.engine.CodecMode;
import io.github.albertus82.acodec.common.resources.ConfigurableMessages;
import io.github.albertus82.acodec.common.resources.Language;
import io.github.albertus82.acodec.common.util.BuildInfo;
import io.github.albertus82.acodec.gui.config.ApplicationConfig;
import io.github.albertus82.acodec.gui.listener.AlgorithmComboSelectionListener;
import io.github.albertus82.acodec.gui.listener.ArmMenuListener;
import io.github.albertus82.acodec.gui.listener.CharsetComboSelectionListener;
import io.github.albertus82.acodec.gui.listener.ExitListener;
import io.github.albertus82.acodec.gui.listener.InputTextModifyListener;
import io.github.albertus82.acodec.gui.listener.ModeRadioSelectionListener;
import io.github.albertus82.acodec.gui.listener.ProcessFileAction;
import io.github.albertus82.acodec.gui.listener.ProcessFileSelectionListener;
import io.github.albertus82.acodec.gui.listener.ShellDropListener;
import io.github.albertus82.acodec.gui.listener.TextCopyKeyListener;
import io.github.albertus82.acodec.gui.listener.TextCopyMenuListener;
import io.github.albertus82.acodec.gui.listener.TextSelectAllKeyListener;
import io.github.albertus82.acodec.gui.listener.TextSelectAllMenuListener;
import io.github.albertus82.acodec.gui.preference.Preference;
import io.github.albertus82.acodec.gui.resources.GuiMessages;
import io.github.albertus82.jface.EnhancedErrorDialog;
import io.github.albertus82.jface.Events;
import io.github.albertus82.jface.Multilanguage;
import io.github.albertus82.jface.SwtUtils;
import io.github.albertus82.jface.closeable.CloseableResource;
import io.github.albertus82.jface.i18n.LocalizedWidgets;
import io.github.albertus82.jface.preference.IPreferencesConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
public class CodecGui extends ApplicationWindow implements Multilanguage {

	private static final String SHELL_MAXIMIZED = "shell.maximized";
	private static final String SHELL_SIZE_X = "shell.size.x";
	private static final String SHELL_SIZE_Y = "shell.size.y";
	private static final String SHELL_LOCATION_X = "shell.location.x";
	private static final String SHELL_LOCATION_Y = "shell.location.y";
	private static final Point POINT_ZERO = new Point(0, 0);

	private static final int TEXT_LIMIT_CHARS = Character.MAX_VALUE;
	private static final int TEXT_HEIGHT_MULTIPLIER = 4;

	private static final String ERROR_PREFIX = "-- ";
	private static final String ERROR_SUFFIX = " --";

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Defaults {
		public static final boolean SHELL_MAXIMIZED = false;
	}

	private final IPreferencesConfiguration configuration = ApplicationConfig.getPreferencesConfiguration();

	@NonNull
	@Setter
	private CodecMode mode = CodecMode.ENCODE;
	@Setter
	private CodecAlgorithm algorithm;
	@NonNull
	@Setter
	private Charset charset = Charset.defaultCharset();

	private MenuBar menuBar;

	@Getter(AccessLevel.NONE)
	private final LocalizedWidgets localizedWidgets = new LocalizedWidgets();

	private Text inputText;
	private Button hideInputTextCheck;
	private Text inputLengthText;

	private Text outputText;
	private Button hideOutputTextCheck;
	private Text outputLengthText;

	private Combo algorithmCombo;
	private Combo charsetCombo;
	private final Map<CodecMode, Button> modeRadios = new EnumMap<>(CodecMode.class);

	private Button processFileButton;

	private DropTarget shellDropTarget;

	@NonNull
	private GuiStatus status = GuiStatus.UNDEFINED;

	/** Shell maximized status. May be null in some circumstances. */
	private Boolean shellMaximized;

	/** Shell size. May be null in some circumstances. */
	private Point shellSize;

	/** Shell location. May be null in some circumstances. */
	private Point shellLocation;

	private CodecGui() {
		super(null);
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		localizeWidget(shell, "gui.message.application.name");
	}

	@Override
	protected void createTrimWidgets(final Shell shell) { /* Not needed */ }

	@Override
	protected Layout getLayout() {
		return GridLayoutFactory.swtDefaults().numColumns(5).create();
	}

	@Override
	protected Control createContents(final Composite parent) {
		menuBar = new MenuBar(this);

		/* Input text */
		final Label inputLabel = localizeWidget(new Label(parent, SWT.NONE), "gui.label.input");
		GridDataFactory.swtDefaults().applyTo(inputLabel);

		inputText = createInputText();

		GridDataFactory.swtDefaults().applyTo(new Label(parent, SWT.NONE)); // Spacer

		hideInputTextCheck = localizeWidget(new Button(parent, SWT.CHECK), "gui.label.input.hide");
		hideInputTextCheck.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		hideInputTextCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				refreshInputTextStyle();
			}
		});

		inputLengthText = new Text(parent, SWT.RIGHT);
		inputLengthText.setEnabled(false);
		try (final CloseableResource<GC> gc = new CloseableResource<>(new GC(inputLengthText))) {
			final int minWidth = gc.getResource().stringExtent(Integer.toString(TEXT_LIMIT_CHARS * 100)).x;
			log.log(Level.FINE, "inputLengthText.minSize.x = {0} px", minWidth);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).minSize(minWidth, SWT.DEFAULT).applyTo(inputLengthText);
		}

		/* Output text */
		final Label outputLabel = localizeWidget(new Label(parent, SWT.NONE), "gui.label.output");
		outputLabel.setLayoutData(new GridData());

		outputText = createOutputText();

		GridDataFactory.swtDefaults().applyTo(new Label(parent, SWT.NONE)); // Spacer

		hideOutputTextCheck = localizeWidget(new Button(parent, SWT.CHECK), "gui.label.output.hide");
		hideOutputTextCheck.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		hideOutputTextCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (!hideInputTextCheck.isDisposed()) {
					if (hideOutputTextCheck.getSelection()) {
						hideInputTextCheck.setEnabled(false);
						hideInputTextCheck.setSelection(true);
						refreshInputTextStyle();
					}
					else {
						hideInputTextCheck.setEnabled(true);
					}
				}
				refreshOutputTextStyle();
			}
		});

		outputLengthText = new Text(parent, SWT.RIGHT);
		outputLengthText.setEnabled(false);
		try (final CloseableResource<GC> gc = new CloseableResource<>(new GC(outputLengthText))) {
			final int minWidth = gc.getResource().stringExtent(Integer.toString(TEXT_LIMIT_CHARS * 100)).x;
			log.log(Level.FINE, "outputLengthText.minSize.x = {0} px", minWidth);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).minSize(minWidth, SWT.DEFAULT).applyTo(outputLengthText);
		}

		/* Codec combo */
		final Label algorithmLabel = localizeWidget(new Label(parent, SWT.NONE), "gui.label.algorithm");
		algorithmLabel.setLayoutData(new GridData());

		algorithmCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		algorithmCombo.setItems(CodecAlgorithm.getNames());
		algorithmCombo.setLayoutData(new GridData());

		/* Charset combo */
		final Label charsetLabel = localizeWidget(new Label(parent, SWT.NONE), "gui.label.charset");
		charsetLabel.setLayoutData(new GridData());

		charsetCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		charsetCombo.setItems(Charset.availableCharsets().keySet().toArray(new String[0]));
		charsetCombo.setText(Charset.defaultCharset().name());
		charsetCombo.setLayoutData(new GridData());

		// Process file button
		processFileButton = localizeWidget(new Button(parent, SWT.NONE), "gui.label.file.process");
		processFileButton.setEnabled(false);
		GridDataFactory.swtDefaults().span(1, 2).align(SWT.BEGINNING, SWT.FILL).applyTo(processFileButton);
		processFileButton.addSelectionListener(new ProcessFileSelectionListener(this));

		/* Mode radio */
		final Label modeLabel = localizeWidget(new Label(parent, SWT.NONE), "gui.label.mode");
		modeLabel.setLayoutData(new GridData());

		final Composite radioComposite = new Composite(parent, SWT.NONE);
		RowLayoutFactory.swtDefaults().applyTo(radioComposite);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(radioComposite);
		for (final CodecMode m : CodecMode.values()) {
			final Button radio = localizeWidget(new Button(radioComposite, SWT.RADIO), "gui.label.mode." + m.getAbbreviation());
			modeRadios.put(m, radio);
			radio.setSelection(m.equals(this.mode));
			radio.addSelectionListener(new ModeRadioSelectionListener(this, radio, m));
		}

		/* Listener */
		algorithmCombo.addSelectionListener(new AlgorithmComboSelectionListener(this));
		charsetCombo.addSelectionListener(new CharsetComboSelectionListener(this));

		/* Drag and drop */
		shellDropTarget = new DropTarget(parent, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
		shellDropTarget.addDropListener(new ShellDropListener(this));

		return parent;
	}

	@Override
	protected void handleShellCloseEvent() {
		final Event event = new Event();
		new ExitListener(this).handleEvent(event);
		if (event.doit) {
			super.handleShellCloseEvent();
		}
	}

	@Override
	protected void constrainShellSize() {
		super.constrainShellSize();
		final Shell shell = getShell();
		shell.pack();

		final Point preferredSize = shell.getSize();
		shell.setMinimumSize(preferredSize);

		final Integer sizeX = configuration.getInt(SHELL_SIZE_X);
		final Integer sizeY = configuration.getInt(SHELL_SIZE_Y);
		if (sizeX != null && sizeY != null) {
			shell.setSize(Math.max(sizeX, preferredSize.x), Math.max(sizeY, preferredSize.y));
		}

		final Integer locationX = configuration.getInt(SHELL_LOCATION_X);
		final Integer locationY = configuration.getInt(SHELL_LOCATION_Y);
		if (locationX != null && locationY != null) {
			if (new Rectangle(locationX, locationY, shell.getSize().x, shell.getSize().y).intersects(shell.getDisplay().getBounds())) {
				shell.setLocation(locationX, locationY);
			}
			else {
				log.log(Level.WARNING, "Illegal shell location ({0}, {1}) for size ({2}).", new Object[] { locationX, locationY, shell.getSize() });
			}
		}

		setMaximizedShellStatus();
	}

	private void setMaximizedShellStatus() {
		if (configuration.getBoolean(SHELL_MAXIMIZED, Defaults.SHELL_MAXIMIZED)) {
			getShell().setMaximized(true);
		}
	}

	@Override
	public int open() {
		final int code = super.open();

		final Shell shell = getShell();
		final UpdateShellStatusListener listener = new UpdateShellStatusListener();
		shell.addListener(SWT.Resize, listener);
		shell.addListener(SWT.Move, listener);
		shell.addListener(SWT.Activate, new MaximizeShellListener());

		if (SwtUtils.isGtk3() == null || SwtUtils.isGtk3()) { // fixes invisible (transparent) shell bug with some Linux distibutions
			setMaximizedShellStatus();
		}

		return code;
	}

	/* GUI entry point */
	public static void main(final String... args) {
		try {
			Display.setAppName(getApplicationName());
			Display.setAppVersion(BuildInfo.getProperty("project.version"));
			Window.setDefaultImages(Images.getAppIconArray());
			start();
		}
		catch (final RuntimeException | Error e) { // NOSONAR Catch Exception instead of Error. Throwable and Error should not be caught (java:S1181)
			log.log(Level.SEVERE, "An unrecoverable error has occurred:", e);
			throw e;
		}
	}

	private static void start() {
		Shell shell = null;
		try {
			ApplicationConfig.initialize(); // Load configuration and initialize the application
			final CodecGui gui = new CodecGui();
			gui.open(); // returns immediately
			shell = gui.getShell(); // to be called after open!
			gui.evaluateInputText();
			loop(shell);
		}
		catch (final RuntimeException e) {
			if (shell != null && shell.isDisposed()) {
				log.log(Level.FINE, "An unrecoverable error has occurred:", e);
				// Do not rethrow, exiting with status OK.
			}
			else {
				EnhancedErrorDialog.openError(shell, getApplicationName(), messages.get("gui.error.fatal"), IStatus.ERROR, e, Images.getAppIconArray());
				throw e;
			}
		}
	}

	private static void loop(@NonNull final Shell shell) {
		final Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.isDisposed() && !display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	public void setInputText(final String text, @NonNull final GuiStatus status) {
		setStatus(status);
		final Listener[] modifyListeners = inputText.getListeners(SWT.Modify);
		if (GuiStatus.DIRTY.equals(status)) {
			for (final Listener modifyListener : modifyListeners) {
				inputText.removeListener(SWT.Modify, modifyListener);
			}
		}
		inputText.setText(text != null ? text : "");
		if (GuiStatus.DIRTY.equals(status)) {
			for (final Listener modifyListener : modifyListeners) {
				inputText.addListener(SWT.Modify, modifyListener);
			}
		}
		refreshInputTextStyle();
		if (GuiStatus.DIRTY.equals(status)) {
			final Color inactiveTextColor = getInactiveTextColor();
			if (!inactiveTextColor.equals(inputText.getForeground())) {
				inputText.setForeground(inactiveTextColor);
			}
		}
		else {
			inputText.setForeground(null);
		}
	}

	public void setOutputText(String text, @NonNull final GuiStatus status) {
		setStatus(status);
		text = text != null ? text : "";
		if (GuiStatus.ERROR.equals(status)) {
			text = new StringBuilder(text).insert(0, ERROR_PREFIX).append(ERROR_SUFFIX).toString();
		}
		outputText.setText(text);
		refreshOutputTextStyle();
		if (EnumSet.of(GuiStatus.ERROR, GuiStatus.DIRTY).contains(status)) {
			final Color inactiveTextColor = getInactiveTextColor();
			if (!inactiveTextColor.equals(outputText.getForeground())) {
				outputText.setForeground(inactiveTextColor);
			}
		}
		else {
			outputText.setForeground(inputText.getForeground()); // Override READ_ONLY style on some platforms.
		}
	}

	private Text createInputText() {
		final Composite composite = new Composite(getShell(), SWT.NONE);
		composite.setLayout(new FillLayout());
		final GridData compositeGridData = GridDataFactory.fillDefaults().grab(true, true).span(4, 1).create();
		composite.setLayoutData(compositeGridData);
		final Text text = new Text(composite, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		configureInputText(text);
		if (TEXT_HEIGHT_MULTIPLIER > 1) {
			compositeGridData.heightHint = text.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
		}
		return text;
	}

	private Text createOutputText() {
		final Composite composite = new Composite(getShell(), SWT.NONE);
		composite.setLayout(new FillLayout());
		final GridData compositeGridData = GridDataFactory.fillDefaults().grab(true, true).span(4, 1).create();
		composite.setLayoutData(compositeGridData);
		final Text text = new Text(composite, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		if (TEXT_HEIGHT_MULTIPLIER > 1) {
			compositeGridData.heightHint = text.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
		}
		text.setForeground(inputText.getForeground()); // Override READ_ONLY style on some platforms.
		text.setBackground(inputText.getBackground()); // Override READ_ONLY style on some platforms.
		configureOutputText(text);
		return text;
	}

	private void configureInputText(@NonNull final Text text) {
		text.setTextLimit(TEXT_LIMIT_CHARS);
		text.addKeyListener(new TextSelectAllKeyListener(text));
		text.addModifyListener(new InputTextModifyListener(this));
	}

	private void configureOutputText(@NonNull final Text text) {
		text.addKeyListener(new TextSelectAllKeyListener(text));
		text.addModifyListener(e -> {
			if (EnumSet.of(GuiStatus.ERROR, GuiStatus.UNDEFINED).contains(status)) {
				outputLengthText.setText("-");
			}
			else {
				outputLengthText.setText(Integer.toString(text.getCharCount()));
			}
		});
	}

	private void refreshInputTextStyle() {
		final boolean mask = !GuiStatus.DIRTY.equals(status) && hideInputTextCheck.getSelection();
		if ((inputText.getStyle() & SWT.PASSWORD) > 0 != mask) {
			final Text oldText = inputText;
			final Composite parent = oldText.getParent();
			final Text newText = new Text(parent, mask ? SWT.BORDER | SWT.PASSWORD : SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
			newText.setText(oldText.getText());
			configureInputText(newText);
			if (mask) {
				newText.addKeyListener(new TextCopyKeyListener(newText));
			}
			inputText = newText;
			oldText.dispose();
			parent.requestLayout();
			parent.layout(); // armhf
		}
	}

	private void refreshOutputTextStyle() {
		final boolean mask = !EnumSet.of(GuiStatus.ERROR, GuiStatus.DIRTY).contains(status) && hideOutputTextCheck.getSelection();
		if ((outputText.getStyle() & SWT.PASSWORD) > 0 != mask) {
			final Text oldText = outputText;
			final Composite parent = oldText.getParent();
			final Text newText = new Text(parent, mask ? SWT.READ_ONLY | SWT.BORDER | SWT.PASSWORD : SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
			newText.setText(oldText.getText());
			newText.setBackground(inputText.getBackground()); // Override READ_ONLY style on some platforms.
			configureOutputText(newText);
			if (mask) {
				createContextMenu(newText);
			}
			outputText = newText;
			oldText.dispose();
			parent.requestLayout();
			parent.layout(); // armhf
		}
	}

	private void createContextMenu(final Text text) {
		text.addKeyListener(new TextCopyKeyListener(text));

		final Menu contextMenu = new Menu(text);
		text.addMenuDetectListener(e -> {
			e.doit = false; // disable default menu
			text.setFocus();
			contextMenu.setVisible(true);
		});

		final MenuItem copyMenuItem = localizedWidgets.putAndReturn(new MenuItem(contextMenu, SWT.PUSH), () -> messages.get("gui.label.context.menu.item.copy") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_COPY)).getKey();
		copyMenuItem.addSelectionListener(new TextCopyMenuListener(text));
		copyMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_COPY);

		new MenuItem(contextMenu, SWT.SEPARATOR);

		final MenuItem selectAllMenuItem = localizedWidgets.putAndReturn(new MenuItem(contextMenu, SWT.PUSH), () -> messages.get("gui.label.context.menu.item.select.all") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SELECT_ALL)).getKey();
		selectAllMenuItem.addSelectionListener(new TextSelectAllMenuListener(text));
		selectAllMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SELECT_ALL);

		contextMenu.addMenuListener(new ArmMenuListener() {
			@Override
			public void menuArmed(final TypedEvent e) {
				if (!text.isDisposed()) {
					copyMenuItem.setEnabled(text.getSelectionCount() > 0);
					selectAllMenuItem.setEnabled(text.getSelectionCount() != text.getCharCount());
				}
			}
		});
	}

	public void evaluateInputText() {
		if (inputText != null && !inputText.isDisposed()) {
			inputText.notifyListeners(SWT.Modify, null);
		}
	}

	public void setLanguage(@NonNull final Language language) {
		messages.setLanguage(language);
		final Shell shell = getShell();
		if (shell != null) {
			shell.setRedraw(false);
			updateLanguage();
			shell.layout(true, true);
			shell.setMinimumSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
			shell.setRedraw(true);
		}
	}

	@Override
	public void updateLanguage() {
		localizedWidgets.resetAllTexts();
		evaluateInputText(); // force the update of any error message
		menuBar.updateLanguage();
	}

	private <T extends Widget> T localizeWidget(@NonNull final T widget, @NonNull final String messageKey) {
		return localizedWidgets.putAndReturn(widget, () -> messages.get(messageKey)).getKey();
	}

	private Color getInactiveTextColor() {
		return getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	}

	public void loadInput() {
		final Shell shell = getShell();
		final FileDialog openDialog = new FileDialog(shell, SWT.OPEN);
		if (algorithm != null && CodecMode.DECODE.equals(mode)) {
			openDialog.setFilterExtensions(ProcessFileAction.buildFilterExtensions(algorithm));
		}
		final String fileName = openDialog.open();
		if (fileName == null || fileName.isEmpty()) {
			return;
		}
		try {
			final Path path = Paths.get(fileName);
			final long fileSize = Files.readAttributes(path, BasicFileAttributes.class).size();
			if (fileSize < 0) {
				throw new IOException();
			}
			if (fileSize > 4 * TEXT_LIMIT_CHARS) { // worst case: UTF-32
				throw new SizeLimitExceededException();
			}
			try (final Reader r = Files.newBufferedReader(path, charset)) {
				final CharBuffer cb = CharBuffer.allocate(TEXT_LIMIT_CHARS);
				r.read(cb);
				if (r.read() != -1) {
					throw new SizeLimitExceededException();
				}
				setInputText(cb.flip().toString(), GuiStatus.UNDEFINED);
			}
		}
		catch (final InvalidPathException e) {
			log.log(Level.INFO, "The path specified is invalid:", e);
			openMessageBox(messages.get("gui.error.file.open.invalid.path"), SWT.ICON_WARNING);
		}
		catch (final FileNotFoundException e) {
			log.log(Level.INFO, "Cannot find the specified file:", e);
			openMessageBox(messages.get("gui.error.file.open.not.found"), SWT.ICON_WARNING);
		}
		catch (final CharacterCodingException e) {
			log.log(Level.FINE, "Cannot load file content with the selected encoding (" + charset + "):", e);
			openMessageBox(messages.get("gui.error.file.open.encoding", charset), SWT.ICON_INFORMATION);
		}
		catch (final SizeLimitExceededException e) {
			log.log(Level.FINE, "The specified file is too large (maximum " + TEXT_LIMIT_CHARS + " characters allowed):", e);
			openMessageBox(messages.get("gui.error.file.open.too.large", TEXT_LIMIT_CHARS), SWT.ICON_INFORMATION);
		}
		catch (final IOException e) {
			log.log(Level.WARNING, "Unexpected error opening the specified file:", e);
			EnhancedErrorDialog.openError(shell, getApplicationName(), messages.get("gui.error.file.open.unexpected"), IStatus.WARNING, e, Images.getAppIconArray());
		}
	}

	public void saveOutput() {
		if (GuiStatus.OK.equals(status)) {
			final ProcessFileAction action = new ProcessFileAction(this);
			action.getDestinationFileName().ifPresent(destinationFileName -> {
				if (!destinationFileName.isEmpty()) {
					action.execute(inputText.getText(), new File(destinationFileName));
				}
			});
		}
	}

	private void setStatus(@NonNull final GuiStatus status) {
		this.status = status;
		menuBar.notifyStatus(status);
	}

	private int openMessageBox(@NonNull final String message, final int style) {
		final MessageBox messageBox = new MessageBox(getShell(), style);
		messageBox.setText(getApplicationName());
		messageBox.setMessage(message);
		return messageBox.open();
	}

	private class MaximizeShellListener implements Listener {
		private boolean firstTime = true;

		@Override
		public void handleEvent(final Event event) {
			logEvent(event);
			if (firstTime && !getShell().isDisposed() && configuration.getBoolean(SHELL_MAXIMIZED, Defaults.SHELL_MAXIMIZED)) {
				firstTime = false;
				getShell().setMaximized(true);
			}
		}
	}

	private static void logEvent(final Event event) {
		log.log(Level.FINE, "{0} {1}", new Object[] { Events.getName(event), event });
	}

	private class UpdateShellStatusListener implements Listener {
		@Override
		public void handleEvent(final Event event) {
			logEvent(event);
			final Shell shell = getShell();
			if (shell != null && !shell.isDisposed()) {
				shellMaximized = shell.getMaximized();
				if (Boolean.FALSE.equals(shellMaximized) && !POINT_ZERO.equals(shell.getSize())) {
					shellSize = shell.getSize();
					shellLocation = shell.getLocation();
				}
			}
			log.log(Level.FINE, "shellMaximized: {0} - shellSize: {1} - shellLocation: {2}", new Object[] { shellMaximized, shellSize, shellLocation });
		}
	}

	public void saveSettings() {
		new Thread(() -> { // don't perform I/O in UI thread
			try {
				configuration.reload(); // make sure the properties are up-to-date
			}
			catch (final IOException e) {
				log.log(Level.WARNING, "Cannot reload configuration:", e);
				return; // abort
			}
			final Properties properties = configuration.getProperties();

			properties.setProperty(Preference.LANGUAGE.getName(), messages.getLanguage().getLocale().getLanguage());

			if (shellMaximized != null) {
				properties.setProperty(SHELL_MAXIMIZED, Boolean.toString(shellMaximized));
			}
			if (shellSize != null) {
				properties.setProperty(SHELL_SIZE_X, Integer.toString(shellSize.x));
				properties.setProperty(SHELL_SIZE_Y, Integer.toString(shellSize.y));
			}
			if (shellLocation != null) {
				properties.setProperty(SHELL_LOCATION_X, Integer.toString(shellLocation.x));
				properties.setProperty(SHELL_LOCATION_Y, Integer.toString(shellLocation.y));
			}

			log.log(Level.FINE, "{0}", configuration);

			try {
				configuration.save(); // save configuration
			}
			catch (final IOException e) {
				log.log(Level.WARNING, "Cannot save configuration:", e);
			}
		}, "Save settings").start();
	}

	public static String getApplicationName() {
		return messages.get("gui.message.application.name");
	}

}
