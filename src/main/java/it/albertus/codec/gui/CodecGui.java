package it.albertus.codec.gui;

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

import it.albertus.codec.Codec;
import it.albertus.codec.engine.CodecAlgorithm;
import it.albertus.codec.engine.CodecMode;

public class CodecGui extends Codec {

	public Shell createShell(final Display display) {
		final Shell shell = new Shell(display);
		shell.setImages(Images.ICONS);
		shell.setText("Codec");
		// shell.setSize(500, 150);
		shell.setLayout(new GridLayout(7, false));

		/* Input text */
		final Label inputLabel = new Label(shell, SWT.NONE);
		inputLabel.setText("Input:");

		final Text inputText = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		inputText.setLayoutData(gridData);
		inputText.addKeyListener(new TextKeyListener(inputText));

		/* Output text */
		final Label outputLabel = new Label(shell, SWT.NONE);
		outputLabel.setText("Output:");

		final Text outputText = new Text(shell, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		outputText.setLayoutData(gridData);
		outputText.setBackground(inputText.getBackground());
		outputText.addKeyListener(new TextKeyListener(outputText));

		/* Codec combo */
		final Label algorithmLabel = new Label(shell, SWT.NONE);
		algorithmLabel.setText("Algorithm:");
		gridData = new GridData();
		algorithmLabel.setLayoutData(gridData);

		final Combo algorithmCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		algorithmCombo.setItems(CodecAlgorithm.getNames());

		/* Mode radio */
		final Label modeLabel = new Label(shell, SWT.NONE);
		modeLabel.setText("Mode:");
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
		aboutButton.setText("About");
		aboutButton.addSelectionListener(new AboutButtonSelectionListener(shell));

		Button exitButton = new Button(shell, SWT.NULL);
		exitButton.setText("Exit");
		exitButton.addSelectionListener(new ExitButtonSelectionListener(shell));

		/* Listener */
		algorithmCombo.addSelectionListener(new AlgorithmComboSelectionListener(engine, algorithmCombo, inputText, modeRadios));
		inputText.addModifyListener(new InputTextModifyListener(engine, inputText, outputText));

		inputText.notifyListeners(SWT.Modify, null);

		shell.pack();
		shell.setMinimumSize(shell.getSize());

		return shell;
	}

}
