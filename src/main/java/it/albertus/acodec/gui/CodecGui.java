package it.albertus.acodec.gui;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;

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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import it.albertus.acodec.common.engine.CodecAlgorithm;
import it.albertus.acodec.common.engine.CodecMode;
import it.albertus.acodec.common.resources.ConfigurableMessages;
import it.albertus.acodec.common.resources.Messages.Language;
import it.albertus.acodec.gui.listener.AlgorithmComboSelectionListener;
import it.albertus.acodec.gui.listener.CharsetComboSelectionListener;
import it.albertus.acodec.gui.listener.CloseListener;
import it.albertus.acodec.gui.listener.InputTextModifyListener;
import it.albertus.acodec.gui.listener.ModeRadioSelectionListener;
import it.albertus.acodec.gui.listener.ProcessFileButtonSelectionListener;
import it.albertus.acodec.gui.listener.ShellDropListener;
import it.albertus.acodec.gui.listener.TextCopySelectionKeyListener;
import it.albertus.acodec.gui.listener.TextSelectAllKeyListener;
import it.albertus.acodec.gui.resources.GuiMessages;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.closeable.CloseableDevice;
import it.albertus.util.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
public class CodecGui implements IShellProvider, Multilanguage {

	private static final int TEXT_LIMIT_CHARS = Character.MAX_VALUE;
	private static final int TEXT_HEIGHT_MULTIPLIER = 4;

	private static final String ERROR_PREFIX = "-- ";
	private static final String ERROR_SUFFIX = " --";

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private CodecMode mode = CodecMode.ENCODE;
	private CodecAlgorithm algorithm;
	private Charset charset = Charset.defaultCharset();

	private final Shell shell;
	private final MenuBar menuBar;

	@Getter(AccessLevel.NONE)
	private final Collection<Label> localizedLabels = new ArrayList<>();
	@Getter(AccessLevel.NONE)
	private final Collection<Button> localizedButtons = new ArrayList<>();

	@NonNull
	@Setter(AccessLevel.PRIVATE)
	private Text inputText;
	private final Button hideInputTextCheck;

	@NonNull
	@Setter(AccessLevel.PRIVATE)
	private Text outputText;
	private final Button hideOutputTextCheck;

	private final Combo algorithmCombo;
	private final Combo charsetCombo;
	private final Map<CodecMode, Button> modeRadios = new EnumMap<>(CodecMode.class);

	private final Button processFileButton;

	private final DropTarget shellDropTarget;

	@NonNull
	private GuiStatus status = GuiStatus.UNDEFINED;

	private CodecGui(final Display display) {
		shell = new Shell(display);
		shell.setImages(Images.getMainIconArray());
		shell.setData("gui.message.application.name");
		shell.setText(messages.get(shell));
		shell.setLayout(new GridLayout(5, false));

		menuBar = new MenuBar(this);

		/* Input text */
		final Label inputLabel = newLocalizedLabel(shell, SWT.NONE, "gui.label.input");
		GridDataFactory.swtDefaults().applyTo(inputLabel);

		inputText = createInputText();

		GridDataFactory.swtDefaults().applyTo(new Label(shell, SWT.NONE)); // Spacer

		hideInputTextCheck = newLocalizedButton(shell, SWT.CHECK, "gui.label.input.hide");
		hideInputTextCheck.setLayoutData(GridDataFactory.swtDefaults().span(4, 1).create());
		hideInputTextCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				refreshInputTextStyle();
			}
		});

		/* Output text */
		final Label outputLabel = newLocalizedLabel(shell, SWT.NONE, "gui.label.output");
		outputLabel.setLayoutData(new GridData());

		outputText = createOutputText();

		GridDataFactory.swtDefaults().applyTo(new Label(shell, SWT.NONE)); // Spacer

		hideOutputTextCheck = newLocalizedButton(shell, SWT.CHECK, "gui.label.output.hide");
		hideOutputTextCheck.setLayoutData(GridDataFactory.swtDefaults().span(4, 1).create());
		hideOutputTextCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				refreshOutputTextStyle();
			}
		});

		/* Codec combo */
		final Label algorithmLabel = newLocalizedLabel(shell, SWT.NONE, "gui.label.algorithm");
		algorithmLabel.setLayoutData(new GridData());

		algorithmCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		algorithmCombo.setItems(CodecAlgorithm.getNames());
		algorithmCombo.setLayoutData(new GridData());

		/* Charset combo */
		final Label charsetLabel = newLocalizedLabel(shell, SWT.NONE, "gui.label.charset");
		charsetLabel.setLayoutData(new GridData());

		charsetCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		charsetCombo.setItems(Charset.availableCharsets().keySet().toArray(new String[0]));
		charsetCombo.setText(Charset.defaultCharset().name());
		charsetCombo.setLayoutData(new GridData());

		// Process file button
		processFileButton = newLocalizedButton(shell, SWT.NONE, "gui.label.file.process");
		processFileButton.setEnabled(false);
		GridDataFactory.swtDefaults().span(1, 2).align(SWT.BEGINNING, SWT.FILL).applyTo(processFileButton);
		processFileButton.addSelectionListener(new ProcessFileButtonSelectionListener(this));

		/* Mode radio */
		final Label modeLabel = newLocalizedLabel(shell, SWT.NONE, "gui.label.mode");
		modeLabel.setLayoutData(new GridData());

		final Composite radioComposite = new Composite(shell, SWT.NONE);
		RowLayoutFactory.swtDefaults().applyTo(radioComposite);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(radioComposite);
		for (final CodecMode m : CodecMode.values()) {
			final Button radio = newLocalizedButton(radioComposite, SWT.RADIO, "gui.label.mode." + m.getAbbreviation());
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

	public static void main(final String... args) {
		Display.setAppName(messages.get("gui.message.application.name"));
		Display.setAppVersion(Version.getNumber());
		try (final CloseableDevice<Display> cd = new CloseableDevice<>(Display.getDefault())) {
			final Display display = cd.getDevice();
			final CodecGui gui = new CodecGui(display);
			final Shell shell = gui.getShell();
			shell.addShellListener(new CloseListener(gui));
			try {
				shell.open();
				gui.refreshOutput();
				loop(shell);
			}
			catch (final Exception e) {
				final String message = e.toString();
				log.log(Level.SEVERE, message, e);
				EnhancedErrorDialog.openError(shell, messages.get("gui.message.error"), message, IStatus.ERROR, e, Images.getMainIconArray());
			}
		}
	}

	private static void loop(final Shell shell) {
		final Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.isDisposed()) {
				try {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
				catch (final NullPointerException e) { // at org.eclipse.swt.widgets.Display.filterMessage(Unknown Source)
					log.log(Level.WARNING, e.toString(), e);
				}
			}
		}
	}

	public void setInputText(final String text, @NonNull final GuiStatus status) {
		setStatus(GuiStatus.UNDEFINED); // Invalidate a possibly previous DIRTY state
		inputText.setText(text != null ? text : "");
		setStatus(status);
		if (GuiStatus.DIRTY.equals(status)) {
			final Color inactiveTextColor = getInactiveTextColor();
			if (!inactiveTextColor.equals(inputText.getForeground())) {
				inputText.setForeground(inactiveTextColor);
			}
		}
		else {
			inputText.setForeground(null);
		}
		refreshInputTextStyle();
	}

	public void setOutputText(String text, @NonNull final GuiStatus status) {
		setStatus(status);
		text = text != null ? text : "";
		if (GuiStatus.ERROR.equals(status)) {
			text = new StringBuilder(text).insert(0, ERROR_PREFIX).append(ERROR_SUFFIX).toString();
		}
		if (EnumSet.of(GuiStatus.ERROR, GuiStatus.DIRTY).contains(status)) {
			final Color inactiveTextColor = getInactiveTextColor();
			if (!inactiveTextColor.equals(outputText.getForeground())) {
				outputText.setForeground(inactiveTextColor);
			}
		}
		else {
			outputText.setForeground(null);
		}
		outputText.setText(text);
		refreshOutputTextStyle();
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

	private void configureInputText(final Text text) {
		text.setTextLimit(TEXT_LIMIT_CHARS);
		text.addKeyListener(TextSelectAllKeyListener.INSTANCE);
		text.addModifyListener(new InputTextModifyListener(this));
	}

	private void configureOutputText(final Text text) {
		text.addKeyListener(TextSelectAllKeyListener.INSTANCE);
	}

	private void refreshInputTextStyle() {
		final boolean mask = !GuiStatus.DIRTY.equals(status) && hideInputTextCheck.getSelection();
		if ((inputText.getStyle() & SWT.PASSWORD) > 0 != mask) {
			final Text oldText = inputText;
			final Composite parent = oldText.getParent();
			final Text newText = new Text(parent, mask ? SWT.BORDER | SWT.PASSWORD : SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
			newText.setText(oldText.getText());
			newText.setForeground(oldText.getForeground());
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
		final boolean mask = GuiStatus.OK.equals(status) && hideOutputTextCheck.getSelection();
		if ((outputText.getStyle() & SWT.PASSWORD) > 0 != mask) {
			final Text oldText = outputText;
			final Composite parent = oldText.getParent();
			final Text newText = new Text(parent, mask ? SWT.READ_ONLY | SWT.BORDER | SWT.PASSWORD : SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
			newText.setText(oldText.getText());
			newText.setForeground(oldText.getForeground());
			newText.setBackground(oldText.getBackground());
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

	public void refreshOutput() {
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
		shell.setText(messages.get(shell));
		for (final Label label : localizedLabels) {
			if (label != null && !label.isDisposed()) {
				label.setText(messages.get(label));
			}
		}
		for (final Button button : localizedButtons) {
			if (button != null && !button.isDisposed()) {
				button.setText(messages.get(button));
			}
		}
		refreshOutput();
		menuBar.updateLanguage();
	}

	private Label newLocalizedLabel(@NonNull final Composite parent, final int style, @NonNull final String messageKey) {
		final Label label = new Label(parent, style);
		label.setData(messageKey);
		label.setText(messages.get(label));
		localizedLabels.add(label);
		return label;
	}

	private Button newLocalizedButton(@NonNull final Composite parent, final int style, @NonNull final String messageKey) {
		final Button button = new Button(parent, style);
		button.setData(messageKey);
		button.setText(messages.get(button));
		localizedButtons.add(button);
		return button;
	}

	private Color getInactiveTextColor() {
		return shell.getDisplay().getSystemColor(SWT.COLOR_RED); // FIXME SWT.COLOR_TITLE_INACTIVE_FOREGROUND
	}

	public enum GuiStatus {
		OK,
		DIRTY,
		ERROR,
		UNDEFINED;
	}

}
