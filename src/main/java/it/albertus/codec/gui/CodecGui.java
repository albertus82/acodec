package it.albertus.codec.gui;

import java.nio.charset.Charset;
import java.util.EnumMap;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import it.albertus.codec.Codec;
import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.gui.listener.AboutListener;
import it.albertus.codec.gui.listener.AlgorithmComboSelectionListener;
import it.albertus.codec.gui.listener.CharsetComboSelectionListener;
import it.albertus.codec.gui.listener.CloseListener;
import it.albertus.codec.gui.listener.InputTextModifyListener;
import it.albertus.codec.gui.listener.ModeRadioSelectionListener;
import it.albertus.codec.gui.listener.ProcessFileSelectionListener;
import it.albertus.codec.gui.listener.TextKeyListener;
import it.albertus.codec.resources.Messages;
import it.albertus.jface.cocoa.CocoaUIEnhancer;

public class CodecGui extends Codec implements IShellProvider {

	private static final int TEXT_LIMIT_CHARS = Character.MAX_VALUE;
	private static final int TEXT_HEIGHT_MULTIPLIER = 4;

	private final Shell shell;
	private final Text inputText;
	private final Text outputText;
	private final Combo algorithmCombo;
	private final Combo charsetCombo;
	private final EnumMap<CodecMode, Button> modeRadios = new EnumMap<CodecMode, Button>(CodecMode.class);
	private final Button aboutButton;
	private final Button processFileButton;

	private boolean dirty = false;

	public CodecGui(final Display display) {
		shell = new Shell(display);
		shell.setImages(Images.MAIN_ICONS);
		shell.setText(Messages.get("msg.application.name"));
		shell.setLayout(new GridLayout(5, false));

		final AboutListener aboutListener = new AboutListener(this);
		if (Util.isCocoa()) {
			try {
				new CocoaUIEnhancer(display).hookApplicationMenu(new CloseListener(this), aboutListener, null);
			}
			catch (final Throwable t) {
				t.printStackTrace();
			}
		}

		/* Input text */
		final Label inputLabel = new Label(shell, SWT.NONE);
		inputLabel.setText(Messages.get("lbl.input"));
		inputLabel.setLayoutData(new GridData());

		inputText = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		{
			final GridData inputTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
			if (TEXT_HEIGHT_MULTIPLIER > 1) {
				inputTextGridData.heightHint = inputText.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
			}
			inputText.setLayoutData(inputTextGridData);
		}
		inputText.setTextLimit(TEXT_LIMIT_CHARS);
		inputText.addKeyListener(new TextKeyListener(inputText));

		/* Output text */
		final Label outputLabel = new Label(shell, SWT.NONE);
		outputLabel.setText(Messages.get("lbl.output"));
		outputLabel.setLayoutData(new GridData());

		outputText = new Text(shell, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		{
			final GridData outputTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
			if (TEXT_HEIGHT_MULTIPLIER > 1) {
				outputTextGridData.heightHint = outputText.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
			}
			outputText.setLayoutData(outputTextGridData);
		}
		if (Util.isWindows()) {
			outputText.setBackground(inputText.getBackground());
		}
		outputText.addKeyListener(new TextKeyListener(outputText));

		/* Codec combo */
		final Label algorithmLabel = new Label(shell, SWT.NONE);
		algorithmLabel.setText(Messages.get("lbl.algorithm"));
		algorithmLabel.setLayoutData(new GridData());

		algorithmCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		algorithmCombo.setItems(CodecAlgorithm.getNames());
		algorithmCombo.setLayoutData(new GridData());

		/* Charset combo */
		final Label charsetLabel = new Label(shell, SWT.NONE);
		charsetLabel.setText(Messages.get("lbl.charset"));
		charsetLabel.setLayoutData(new GridData());

		charsetCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		charsetCombo.setItems(Charset.availableCharsets().keySet().toArray(new String[0]));
		charsetCombo.setText(Charset.defaultCharset().name());
		charsetCombo.setLayoutData(new GridData());

		/* File */
		processFileButton = new Button(shell, SWT.NULL);
		processFileButton.setEnabled(false);
		processFileButton.setText(Messages.get("lbl.file.process"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(processFileButton);
		processFileButton.addSelectionListener(new ProcessFileSelectionListener(this));

		/* Mode radio */
		final Label modeLabel = new Label(shell, SWT.NONE);
		modeLabel.setText(Messages.get("lbl.mode"));
		modeLabel.setLayoutData(new GridData());

		final Composite radioComposite = new Composite(shell, SWT.NONE);
		radioComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		radioComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		for (final CodecMode mode : CodecMode.values()) {
			final Button radio = new Button(radioComposite, SWT.RADIO);
			radio.setSelection(getEngine().getMode().equals(mode));
			radio.setText(mode.getName());
			radio.addSelectionListener(new ModeRadioSelectionListener(this, radio, mode));
			modeRadios.put(mode, radio);
		}

		aboutButton = new Button(shell, SWT.NULL);
		aboutButton.setText(Messages.get("lbl.about"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(aboutButton);
		aboutButton.addSelectionListener(aboutListener);

		/* Listener */
		algorithmCombo.addSelectionListener(new AlgorithmComboSelectionListener(this));
		charsetCombo.addSelectionListener(new CharsetComboSelectionListener(this));
		inputText.addModifyListener(new InputTextModifyListener(this));

		inputText.notifyListeners(SWT.Modify, null);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
	}

	public void enableControls() {
		inputText.setEnabled(true);
		outputText.setEnabled(true);
		algorithmCombo.setEnabled(true);
		charsetCombo.setEnabled(true);
		processFileButton.setEnabled(true);
		for (final Button radio : modeRadios.values()) {
			radio.setEnabled(true);
		}
		algorithmCombo.notifyListeners(SWT.Selection, null);
	}

	public void disableControls() {
		inputText.setEnabled(false);
		outputText.setEnabled(false);
		algorithmCombo.setEnabled(false);
		charsetCombo.setEnabled(false);
		processFileButton.setEnabled(false);
		for (final Button radio : modeRadios.values()) {
			radio.setEnabled(false);
		}
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	public Text getInputText() {
		return inputText;
	}

	public Text getOutputText() {
		return outputText;
	}

	public Combo getAlgorithmCombo() {
		return algorithmCombo;
	}

	public Combo getCharsetCombo() {
		return charsetCombo;
	}

	public EnumMap<CodecMode, Button> getModeRadios() {
		return modeRadios;
	}

	public Button getAboutButton() {
		return aboutButton;
	}

	public Button getProcessFileButton() {
		return processFileButton;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

}
