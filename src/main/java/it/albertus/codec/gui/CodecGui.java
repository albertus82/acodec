package it.albertus.codec.gui;

import it.albertus.codec.Codec;
import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.gui.listener.AboutSelectionListener;
import it.albertus.codec.gui.listener.AlgorithmComboSelectionListener;
import it.albertus.codec.gui.listener.CloseSelectionListener;
import it.albertus.codec.gui.listener.InputTextModifyListener;
import it.albertus.codec.gui.listener.ModeRadioSelectionListener;
import it.albertus.codec.gui.listener.TextKeyListener;
import it.albertus.codec.resources.Resources;

import java.util.EnumMap;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CodecGui extends Codec implements IShellProvider {

	private static final int TEXT_LIMIT_CHARS = Character.MAX_VALUE;
	private static final int TEXT_HEIGHT_MULTIPLIER = 2;

	private Shell shell;
	private Text inputText;
	private Text outputText;
	private Combo algorithmCombo;
	private final EnumMap<CodecMode, Button> modeRadios = new EnumMap<CodecMode, Button>(CodecMode.class);
	private Button aboutButton;
	private Button closeButton;

	public Shell createShell(final Display display) {
		shell = new Shell(display);
		shell.setImages(Images.MAIN_ICONS);
		shell.setText(Resources.get("msg.application.name"));
		shell.setLayout(new GridLayout(7, false));

		/* Input text */
		final Label inputLabel = new Label(shell, SWT.NONE);
		inputLabel.setText(Resources.get("lbl.input"));

		inputText = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
		if (TEXT_HEIGHT_MULTIPLIER > 1) {
			gridData.heightHint = inputText.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
		}
		inputText.setLayoutData(gridData);
		inputText.setTextLimit(TEXT_LIMIT_CHARS);
		inputText.addKeyListener(new TextKeyListener(inputText));

		/* Output text */
		final Label outputLabel = new Label(shell, SWT.NONE);
		outputLabel.setText(Resources.get("lbl.output"));

		outputText = new Text(shell, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
		if (TEXT_HEIGHT_MULTIPLIER > 1) {
			gridData.heightHint = outputText.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
		}
		outputText.setLayoutData(gridData);
		outputText.setBackground(inputText.getBackground());
		outputText.addKeyListener(new TextKeyListener(outputText));

		/* Codec combo */
		final Label algorithmLabel = new Label(shell, SWT.NONE);
		algorithmLabel.setText(Resources.get("lbl.algorithm"));
		gridData = new GridData();
		algorithmLabel.setLayoutData(gridData);

		algorithmCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		algorithmCombo.setItems(CodecAlgorithm.getNames());

		/* Mode radio */
		final Label modeLabel = new Label(shell, SWT.NONE);
		modeLabel.setText(Resources.get("lbl.mode"));
		gridData = new GridData();
		modeLabel.setLayoutData(gridData);

		for (final CodecMode mode : CodecMode.values()) {
			final Button radio = new Button(shell, SWT.RADIO);
			radio.setSelection(getEngine().getMode().equals(mode));
			radio.setText(mode.getName());
			radio.addSelectionListener(new ModeRadioSelectionListener(this, radio, mode));
			modeRadios.put(mode, radio);
		}

		/* Buttons */
		aboutButton = new Button(shell, SWT.NULL);
		aboutButton.setText(Resources.get("lbl.about"));
		aboutButton.addSelectionListener(new AboutSelectionListener(this));

		closeButton = new Button(shell, SWT.NULL);
		closeButton.setText(Resources.get("lbl.close"));
		closeButton.addSelectionListener(new CloseSelectionListener(this));

		/* Listener */
		algorithmCombo.addSelectionListener(new AlgorithmComboSelectionListener(this));
		inputText.addModifyListener(new InputTextModifyListener(this));

		inputText.notifyListeners(SWT.Modify, null);

		shell.pack();
		shell.setMinimumSize(shell.getSize());

		return shell;
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

	public EnumMap<CodecMode, Button> getModeRadios() {
		return modeRadios;
	}

	public Button getAboutButton() {
		return aboutButton;
	}

	public Button getCloseButton() {
		return closeButton;
	}

}
