package it.albertus.acodec.gui;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import it.albertus.acodec.gui.listener.TextCopyKeyListener;
import it.albertus.acodec.gui.listener.TextSelectAllKeyListener;
import it.albertus.acodec.gui.resources.GuiMessages;
import it.albertus.jface.EnhancedErrorDialog;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.closeable.CloseableDevice;
import it.albertus.util.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
@Getter
@Setter
public class CodecGui implements IShellProvider, Multilanguage {

	private static final int TEXT_LIMIT_CHARS = Character.MAX_VALUE;
	private static final int TEXT_HEIGHT_MULTIPLIER = 4;

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private CodecMode mode = CodecMode.ENCODE;
	private CodecAlgorithm algorithm;
	private Charset charset = Charset.defaultCharset();

	private final Shell shell;
	private final MenuBar menuBar;

	@Getter(AccessLevel.NONE)
	private final Collection<Label> labels = new ArrayList<>();

	private Text inputText;
	private final Button hideInputTextCheck;
	private Text outputText;
	private final Button hideOutputTextCheck;
	private final Combo algorithmCombo;
	private final Combo charsetCombo;
	private final Map<CodecMode, Button> modeRadios = new EnumMap<>(CodecMode.class);
	private final Button processFileButton;
	private final DropTarget shellDropTarget;

	private boolean dirty = false;
	private boolean error = false;

	private CodecGui(final Display display) {
		shell = new Shell(display);
		shell.setImages(Images.getMainIconArray());
		shell.setData("gui.message.application.name");
		shell.setText(messages.get(shell));
		shell.setLayout(new GridLayout(5, false));

		menuBar = new MenuBar(this);

		/* Input text */
		final Label inputLabel = new Label(shell, SWT.NONE);
		labels.add(inputLabel);
		inputLabel.setData("gui.label.input");
		inputLabel.setText(messages.get(inputLabel));
		GridDataFactory.swtDefaults().applyTo(inputLabel);

		inputText = createInputText();

		new Label(shell, SWT.NONE).setLayoutData(GridDataFactory.swtDefaults().create()); // Spacer

		hideInputTextCheck = new Button(shell, SWT.CHECK);
		hideInputTextCheck.setData("gui.label.input.hide");
		hideInputTextCheck.setText(messages.get(hideInputTextCheck));
		hideInputTextCheck.setLayoutData(GridDataFactory.swtDefaults().span(4, 1).create());
		hideInputTextCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				refreshInputText();
			}
		});

		/* Output text */
		final Label outputLabel = new Label(shell, SWT.NONE);
		labels.add(outputLabel);
		outputLabel.setData("gui.label.output");
		outputLabel.setText(messages.get(outputLabel));
		outputLabel.setLayoutData(new GridData());

		outputText = createOutputText();

		new Label(shell, SWT.NONE).setLayoutData(GridDataFactory.swtDefaults().create()); // Spacer

		hideOutputTextCheck = new Button(shell, SWT.CHECK);
		hideOutputTextCheck.setData("gui.label.output.hide");
		hideOutputTextCheck.setText(messages.get(hideOutputTextCheck));
		hideOutputTextCheck.setLayoutData(GridDataFactory.swtDefaults().span(4, 1).create());
		hideOutputTextCheck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				refreshOutputText();
			}
		});

		/* Codec combo */
		final Label algorithmLabel = new Label(shell, SWT.NONE);
		labels.add(algorithmLabel);
		algorithmLabel.setData("gui.label.algorithm");
		algorithmLabel.setText(messages.get(algorithmLabel));
		algorithmLabel.setLayoutData(new GridData());

		algorithmCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		algorithmCombo.setItems(CodecAlgorithm.getNames());
		algorithmCombo.setLayoutData(new GridData());

		/* Charset combo */
		final Label charsetLabel = new Label(shell, SWT.NONE);
		labels.add(charsetLabel);
		charsetLabel.setData("gui.label.charset");
		charsetLabel.setText(messages.get(charsetLabel));
		charsetLabel.setLayoutData(new GridData());

		charsetCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		charsetCombo.setItems(Charset.availableCharsets().keySet().toArray(new String[0]));
		charsetCombo.setText(Charset.defaultCharset().name());
		charsetCombo.setLayoutData(new GridData());

		// Process file button
		processFileButton = new Button(shell, SWT.NONE);
		processFileButton.setEnabled(false);
		processFileButton.setData("gui.label.file.process");
		processFileButton.setText(messages.get(processFileButton));
		GridDataFactory.swtDefaults().span(1, 2).align(SWT.BEGINNING, SWT.FILL).applyTo(processFileButton);
		processFileButton.addSelectionListener(new ProcessFileButtonSelectionListener(this));

		/* Mode radio */
		final Label modeLabel = new Label(shell, SWT.NONE);
		labels.add(modeLabel);
		modeLabel.setData("gui.label.mode");
		modeLabel.setText(messages.get(modeLabel));
		modeLabel.setLayoutData(new GridData());

		final Composite radioComposite = new Composite(shell, SWT.NONE);
		RowLayoutFactory.swtDefaults().applyTo(radioComposite);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(radioComposite);
		for (final CodecMode m : CodecMode.values()) {
			final Button radio = new Button(radioComposite, SWT.RADIO);
			radio.setSelection(m.equals(this.mode));
			radio.setText(messages.get("gui.label.mode." + m.getAbbreviation()));
			radio.addSelectionListener(new ModeRadioSelectionListener(this, radio, m));
			modeRadios.put(m, radio);
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
					log.log(Level.FINE, e.toString(), e);
				}
			}
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

	private void refreshInputText() {
		final boolean mask = hideInputTextCheck.getSelection();
		if ((inputText.getStyle() & SWT.PASSWORD) > 0 != mask) {
			final Text oldText = inputText;
			final Composite parent = oldText.getParent();
			final Text newText = new Text(parent, mask ? SWT.BORDER | SWT.PASSWORD : SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
			configureInputText(newText);
			if (mask) {
				newText.addKeyListener(new TextCopyKeyListener(newText));
			}
			newText.setText(oldText.getText());
			inputText = newText;
			oldText.dispose();
			parent.requestLayout();
		}
	}

	private void configureInputText(final Text text) {
		text.setTextLimit(TEXT_LIMIT_CHARS);
		text.addKeyListener(new TextSelectAllKeyListener(text));
		text.addModifyListener(new InputTextModifyListener(this));
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
		configureOutputText(text);
		return text;
	}

	public void refreshOutputText() {
		final boolean mask = !error && !dirty && hideOutputTextCheck.getSelection();
		if ((outputText.getStyle() & SWT.PASSWORD) > 0 != mask) {
			final Text oldText = outputText;
			final Composite parent = oldText.getParent();
			final Text newText = new Text(parent, mask ? SWT.READ_ONLY | SWT.BORDER | SWT.PASSWORD : SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
			configureOutputText(newText);
			if (mask) {
				newText.addKeyListener(new TextCopyKeyListener(newText));
			}
			newText.setText(oldText.getText());
			outputText = newText;
			oldText.dispose();
			parent.requestLayout();
		}
	}

	private void configureOutputText(final Text text) {
		if (Util.isWindows()) {
			text.setBackground(inputText.getBackground());
		}
		text.addKeyListener(new TextSelectAllKeyListener(text));
	}

	public void refreshOutput() {
		if (inputText != null && !inputText.isDisposed()) {
			inputText.notifyListeners(SWT.Modify, null);
		}
	}

	public void setLanguage(final Language language) {
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
		for (final Label label : labels) {
			label.setText(messages.get(label));
		}
		hideInputTextCheck.setText(messages.get(hideInputTextCheck));
		processFileButton.setText(messages.get(processFileButton));
		for (final Entry<CodecMode, Button> entry : modeRadios.entrySet()) {
			entry.getValue().setText(messages.get("gui.label.mode." + entry.getKey().getAbbreviation()));
		}
		refreshOutput();
		menuBar.updateLanguage();
	}

}
