package it.albertus.codec.gui;

import it.albertus.codec.Codec;
import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.gui.listener.AboutSelectionListener;
import it.albertus.codec.gui.listener.AlgorithmComboSelectionListener;
import it.albertus.codec.gui.listener.CharsetComboSelectionListener;
import it.albertus.codec.gui.listener.InputTextModifyListener;
import it.albertus.codec.gui.listener.ModeRadioSelectionListener;
import it.albertus.codec.gui.listener.ProcessFileSelectionListener;
import it.albertus.codec.gui.listener.TextKeyListener;
import it.albertus.codec.resources.Resources;

import java.nio.charset.Charset;
import java.util.EnumMap;

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

	public CodecGui(final Display display) {
		shell = new Shell(display);
		shell.setImages(Images.MAIN_ICONS);
		shell.setText(Resources.get("msg.application.name"));
		shell.setLayout(new GridLayout(5, false));

		/* Input text */
		final Label inputLabel = new Label(shell, SWT.NONE);
		inputLabel.setText(Resources.get("lbl.input"));
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
		outputLabel.setText(Resources.get("lbl.output"));
		outputLabel.setLayoutData(new GridData());

		outputText = new Text(shell, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		{
			final GridData outputTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
			if (TEXT_HEIGHT_MULTIPLIER > 1) {
				outputTextGridData.heightHint = outputText.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
			}
			outputText.setLayoutData(outputTextGridData);
		}
		outputText.setBackground(inputText.getBackground());
		outputText.addKeyListener(new TextKeyListener(outputText));

		/* Codec combo */
		final Label algorithmLabel = new Label(shell, SWT.NONE);
		algorithmLabel.setText(Resources.get("lbl.algorithm"));
		algorithmLabel.setLayoutData(new GridData());

		algorithmCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		algorithmCombo.setItems(CodecAlgorithm.getNames());
		algorithmCombo.setLayoutData(new GridData());

		/* Charset combo */
		final Label charsetLabel = new Label(shell, SWT.NONE);
		charsetLabel.setText(Resources.get("lbl.charset"));
		charsetLabel.setLayoutData(new GridData());

		charsetCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		charsetCombo.setItems(Charset.availableCharsets().keySet().toArray(new String[0]));
		charsetCombo.setText(Charset.defaultCharset().name());
		charsetCombo.setLayoutData(new GridData());

		/* File */
		processFileButton = new Button(shell, SWT.NULL);
		processFileButton.setEnabled(false);
		processFileButton.setText(Resources.get("lbl.file.process"));
		processFileButton.setLayoutData(new GridData());
		processFileButton.addSelectionListener(new ProcessFileSelectionListener(this));

		/* Mode radio */
		final Label modeLabel = new Label(shell, SWT.NONE);
		modeLabel.setText(Resources.get("lbl.mode"));
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
		aboutButton.setText(Resources.get("lbl.about"));
		aboutButton.setLayoutData(new GridData());
		aboutButton.addSelectionListener(new AboutSelectionListener(this));

		/* Listener */
		algorithmCombo.addSelectionListener(new AlgorithmComboSelectionListener(this));
		charsetCombo.addSelectionListener(new CharsetComboSelectionListener(this));
		inputText.addModifyListener(new InputTextModifyListener(this));

		inputText.notifyListeners(SWT.Modify, null);

		shell.pack();
		shell.setMinimumSize(shell.getSize());
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

}
