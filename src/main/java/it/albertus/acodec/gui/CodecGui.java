package it.albertus.acodec.gui;

import static it.albertus.acodec.gui.GuiStatus.DIRTY;
import static it.albertus.acodec.gui.GuiStatus.ERROR;
import static it.albertus.acodec.gui.GuiStatus.OK;
import static it.albertus.acodec.gui.GuiStatus.UNDEFINED;

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
import java.util.logging.Level;

import javax.naming.SizeLimitExceededException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import it.albertus.acodec.common.engine.CodecAlgorithm;
import it.albertus.acodec.common.engine.CodecMode;
import it.albertus.acodec.common.resources.ConfigurableMessages;
import it.albertus.acodec.common.resources.Language;
import it.albertus.acodec.gui.listener.AlgorithmComboSelectionListener;
import it.albertus.acodec.gui.listener.CharsetComboSelectionListener;
import it.albertus.acodec.gui.listener.ExitListener;
import it.albertus.acodec.gui.listener.InputTextModifyListener;
import it.albertus.acodec.gui.listener.ModeRadioSelectionListener;
import it.albertus.acodec.gui.listener.ProcessFileAction;
import it.albertus.acodec.gui.listener.ProcessFileSelectionListener;
import it.albertus.acodec.gui.listener.ShellDropListener;
import it.albertus.acodec.gui.listener.TextCopySelectionKeyListener;
import it.albertus.acodec.gui.listener.TextSelectAllKeyListener;
import it.albertus.acodec.gui.resources.GuiMessages;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.closeable.CloseableDevice;
import it.albertus.jface.closeable.CloseableResource;
import it.albertus.jface.i18n.LocalizedWidgets;
import it.albertus.util.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
public class CodecGui implements IShellProvider, Multilanguage {

	private static final int TEXT_LIMIT_CHARS = Character.MAX_VALUE;
	private static final int TEXT_HEIGHT_MULTIPLIER = 4;

	private static final String ERROR_PREFIX = "-- ";
	private static final String ERROR_SUFFIX = " --";

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	@NonNull
	@Setter
	private CodecMode mode = CodecMode.ENCODE;
	@Setter
	private CodecAlgorithm algorithm;
	@NonNull
	@Setter
	private Charset charset = Charset.defaultCharset();

	private final Shell shell;
	private final MenuBar menuBar;

	@Getter(AccessLevel.NONE)
	private final LocalizedWidgets localizedWidgets = new LocalizedWidgets();

	@NonNull
	private Text inputText;
	private final Button hideInputTextCheck;
	private final Text inputLengthText;

	@NonNull
	private Text outputText;
	private final Button hideOutputTextCheck;
	private final Text outputLengthText;

	private final Combo algorithmCombo;
	private final Combo charsetCombo;
	private final Map<CodecMode, Button> modeRadios = new EnumMap<>(CodecMode.class);

	private final Button processFileButton;

	private final DropTarget shellDropTarget;

	@NonNull
	private GuiStatus status = UNDEFINED;

	private CodecGui(final Display display) {
		shell = localizeWidget(new Shell(display), "gui.message.application.name");
		shell.setImages(Images.getAppIconArray());
		shell.setLayout(new GridLayout(5, false));
		shell.addShellListener(new ExitListener(this));

		menuBar = new MenuBar(this);

		/* Input text */
		final Label inputLabel = localizeWidget(new Label(shell, SWT.NONE), "gui.label.input");
		GridDataFactory.swtDefaults().applyTo(inputLabel);

		inputText = createInputText();

		GridDataFactory.swtDefaults().applyTo(new Label(shell, SWT.NONE)); // Spacer

		hideInputTextCheck = localizeWidget(new Button(shell, SWT.CHECK), "gui.label.input.hide");
		hideInputTextCheck.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		hideInputTextCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				refreshInputTextStyle();
			}
		});

		inputLengthText = new Text(shell, SWT.RIGHT);
		inputLengthText.setEnabled(false);
		try (final CloseableResource<GC> gc = new CloseableResource<>(new GC(inputLengthText))) {
			final int minWidth = gc.getResource().stringExtent(Integer.toString(TEXT_LIMIT_CHARS * 100)).x;
			log.log(Level.FINE, "inputLengthText.minSize.x = {0} px", minWidth);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).minSize(minWidth, SWT.DEFAULT).applyTo(inputLengthText);
		}

		/* Output text */
		final Label outputLabel = localizeWidget(new Label(shell, SWT.NONE), "gui.label.output");
		outputLabel.setLayoutData(new GridData());

		outputText = createOutputText();

		GridDataFactory.swtDefaults().applyTo(new Label(shell, SWT.NONE)); // Spacer

		hideOutputTextCheck = localizeWidget(new Button(shell, SWT.CHECK), "gui.label.output.hide");
		hideOutputTextCheck.setLayoutData(GridDataFactory.swtDefaults().span(3, 1).create());
		hideOutputTextCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				refreshOutputTextStyle();
			}
		});

		outputLengthText = new Text(shell, SWT.RIGHT);
		outputLengthText.setEnabled(false);
		try (final CloseableResource<GC> gc = new CloseableResource<>(new GC(outputLengthText))) {
			final int minWidth = gc.getResource().stringExtent(Integer.toString(TEXT_LIMIT_CHARS * 100)).x;
			log.log(Level.FINE, "outputLengthText.minSize.x = {0} px", minWidth);
			GridDataFactory.swtDefaults().grab(true, false).align(SWT.END, SWT.CENTER).minSize(minWidth, SWT.DEFAULT).applyTo(outputLengthText);
		}

		/* Codec combo */
		final Label algorithmLabel = localizeWidget(new Label(shell, SWT.NONE), "gui.label.algorithm");
		algorithmLabel.setLayoutData(new GridData());

		algorithmCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		algorithmCombo.setItems(CodecAlgorithm.getNames());
		algorithmCombo.setLayoutData(new GridData());

		/* Charset combo */
		final Label charsetLabel = localizeWidget(new Label(shell, SWT.NONE), "gui.label.charset");
		charsetLabel.setLayoutData(new GridData());

		charsetCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		charsetCombo.setItems(Charset.availableCharsets().keySet().toArray(new String[0]));
		charsetCombo.setText(Charset.defaultCharset().name());
		charsetCombo.setLayoutData(new GridData());

		// Process file button
		processFileButton = localizeWidget(new Button(shell, SWT.NONE), "gui.label.file.process");
		processFileButton.setEnabled(false);
		GridDataFactory.swtDefaults().span(1, 2).align(SWT.BEGINNING, SWT.FILL).applyTo(processFileButton);
		processFileButton.addSelectionListener(new ProcessFileSelectionListener(this));

		/* Mode radio */
		final Label modeLabel = localizeWidget(new Label(shell, SWT.NONE), "gui.label.mode");
		modeLabel.setLayoutData(new GridData());

		final Composite radioComposite = new Composite(shell, SWT.NONE);
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
		shellDropTarget = new DropTarget(shell, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
		shellDropTarget.addDropListener(new ShellDropListener(this));

		shell.pack();
		shell.setMinimumSize(shell.getSize());
	}

	/* GUI entry point */
	public static void main(final String... args) {
		try {
			Display.setAppName(getApplicationName());
			Display.setAppVersion(Version.getNumber());
			start();
		}
		catch (final RuntimeException | Error e) { // NOSONAR Catch Exception instead of Error. Throwable and Error should not be caught (java:S1181)
			log.log(Level.SEVERE, "An unrecoverable error has occurred:", e);
			throw e;
		}
	}

	private static void start() {
		try (final CloseableDevice<Display> cd = new CloseableDevice<>(Display.getDefault())) {
			Shell shell = null;
			try {
				final CodecGui gui = new CodecGui(cd.getDevice());
				shell = gui.getShell();
				shell.open();
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
		} // Display is disposed before the catch!
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
		if (DIRTY.equals(status)) {
			for (final Listener modifyListener : modifyListeners) {
				inputText.removeListener(SWT.Modify, modifyListener);
			}
		}
		inputText.setText(text != null ? text : "");
		if (DIRTY.equals(status)) {
			for (final Listener modifyListener : modifyListeners) {
				inputText.addListener(SWT.Modify, modifyListener);
			}
		}
		refreshInputTextStyle();
		if (DIRTY.equals(status)) {
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
		if (ERROR.equals(status)) {
			text = new StringBuilder(text).insert(0, ERROR_PREFIX).append(ERROR_SUFFIX).toString();
		}
		outputText.setText(text);
		refreshOutputTextStyle();
		if (EnumSet.of(ERROR, DIRTY).contains(status)) {
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
		final Composite composite = new Composite(shell, SWT.NONE);
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
		final Composite composite = new Composite(shell, SWT.NONE);
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
		text.addKeyListener(TextSelectAllKeyListener.INSTANCE);
		text.addModifyListener(new InputTextModifyListener(this));
	}

	private void configureOutputText(@NonNull final Text text) {
		text.addKeyListener(TextSelectAllKeyListener.INSTANCE);
		text.addModifyListener(e -> {
			if (EnumSet.of(ERROR, UNDEFINED).contains(status)) {
				outputLengthText.setText("-");
			}
			else {
				outputLengthText.setText(Integer.toString(text.getCharCount()));
			}
		});
	}

	private void refreshInputTextStyle() {
		final boolean mask = !DIRTY.equals(status) && hideInputTextCheck.getSelection();
		if ((inputText.getStyle() & SWT.PASSWORD) > 0 != mask) {
			final Text oldText = inputText;
			final Composite parent = oldText.getParent();
			final Text newText = new Text(parent, mask ? SWT.BORDER | SWT.PASSWORD : SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
			newText.setText(oldText.getText());
			configureInputText(newText);
			if (mask) {
				newText.addKeyListener(TextCopySelectionKeyListener.INSTANCE);
			}
			inputText = newText;
			oldText.dispose();
			parent.requestLayout();
			parent.layout(); // armhf
		}
	}

	private void refreshOutputTextStyle() {
		final boolean mask = !EnumSet.of(ERROR, DIRTY).contains(status) && hideOutputTextCheck.getSelection();
		if ((outputText.getStyle() & SWT.PASSWORD) > 0 != mask) {
			final Text oldText = outputText;
			final Composite parent = oldText.getParent();
			final Text newText = new Text(parent, mask ? SWT.READ_ONLY | SWT.BORDER | SWT.PASSWORD : SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
			newText.setText(oldText.getText());
			newText.setBackground(inputText.getBackground()); // Override READ_ONLY style on some platforms.
			configureOutputText(newText);
			if (mask) {
				newText.addKeyListener(TextCopySelectionKeyListener.INSTANCE);
			}
			outputText = newText;
			oldText.dispose();
			parent.requestLayout();
			parent.layout(); // armhf
		}
	}

	public void evaluateInputText() {
		if (inputText != null && !inputText.isDisposed()) {
			inputText.notifyListeners(SWT.Modify, null);
		}
	}

	public void setLanguage(@NonNull final Language language) {
		messages.setLanguage(language);
		shell.setRedraw(false);
		updateLanguage();
		shell.layout(true, true);
		shell.setMinimumSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
		shell.setRedraw(true);
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
		return shell.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	}

	public void loadInput() {
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
				setInputText(cb.flip().toString(), UNDEFINED);
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
		if (OK.equals(status)) {
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
		final MessageBox messageBox = new MessageBox(shell, style);
		messageBox.setText(getApplicationName());
		messageBox.setMessage(message);
		return messageBox.open();
	}

	public static String getApplicationName() {
		return messages.get("gui.message.application.name");
	}

}
