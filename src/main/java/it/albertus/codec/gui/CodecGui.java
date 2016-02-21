package it.albertus.codec.gui;

import it.albertus.codec.Codec;
import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.resources.Resources;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CodecGui extends Codec {

	private static final int TEXT_LIMIT_CHARS = Character.MAX_VALUE;
	private static final int TEXT_HEIGHT_MULTIPLIER = 2;

	public Shell createShell(final Display display) {
		final Shell shell = new Shell(display);
		shell.setImages(Images.ICONS);
		shell.setText(Resources.get("lbl.title"));
		shell.setLayout(new GridLayout(7, false));

		/* Input text */
		final Label inputLabel = new Label(shell, SWT.NONE);
		inputLabel.setText(Resources.get("lbl.input"));

		final Text inputText = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		if (TEXT_HEIGHT_MULTIPLIER > 1) {
			gridData.heightHint = inputText.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
		}
		inputText.setLayoutData(gridData);
		inputText.setTextLimit(TEXT_LIMIT_CHARS);
		inputText.addKeyListener(new TextKeyListener(inputText));

		/* Output text */
		final Label outputLabel = new Label(shell, SWT.NONE);
		outputLabel.setText(Resources.get("lbl.output"));

		final Text outputText = new Text(shell, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
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

		final Combo algorithmCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		algorithmCombo.setItems(CodecAlgorithm.getNames());

		/* Mode radio */
		final Label modeLabel = new Label(shell, SWT.NONE);
		modeLabel.setText(Resources.get("lbl.mode"));
		gridData = new GridData();
		modeLabel.setLayoutData(gridData);

		final Map<CodecMode, Button> modeRadios = new EnumMap<CodecMode, Button>(CodecMode.class);
		for (CodecMode mode : CodecMode.values()) {
			Button radio = new Button(shell, SWT.RADIO);
			radio.setSelection(engine.getMode().equals(mode));
			radio.setText(mode.getName());
			radio.addSelectionListener(new ModeRadioSelectionListener(engine, radio, mode, inputText));
			modeRadios.put(mode, radio);
		}

		/* Buttons */
		Button aboutButton = new Button(shell, SWT.NULL);
		aboutButton.setText(Resources.get("lbl.about"));
		aboutButton.addSelectionListener(new AboutButtonSelectionListener(shell));

		Button closeButton = new Button(shell, SWT.NULL);
		closeButton.setText(Resources.get("lbl.close"));
		closeButton.addSelectionListener(new CloseButtonSelectionListener(shell));

		/* Listener */
		algorithmCombo.addSelectionListener(new AlgorithmComboSelectionListener(engine, algorithmCombo, inputText, modeRadios));
		inputText.addModifyListener(new InputTextModifyListener(engine, inputText, outputText));

		inputText.notifyListeners(SWT.Modify, null);

		shell.pack();
		shell.setMinimumSize(shell.getSize());

		return shell;
	}

}
