package it.albertus.codec;

import it.albertus.codec.engine.CodecEngine;
import it.albertus.codec.engine.CodecMode;
import it.albertus.codec.engine.CodecType;
import it.albertus.codec.gui.AboutButtonSelectionListener;
import it.albertus.codec.gui.CodecComboSelectionListener;
import it.albertus.codec.gui.ExitButtonSelectionListener;
import it.albertus.codec.gui.Images;
import it.albertus.codec.gui.InputTextModifyListener;
import it.albertus.codec.gui.ModeRadioSelectionListener;
import it.albertus.codec.gui.TextKeyListener;

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

public class Codec {

	private final CodecEngine engine;

	public Codec() {
		this.engine = new CodecEngine();
	}

	public static void main(final String[] args) {
		Codec app = new Codec();
		if (args.length > 0) {
			// TODO Console version
		}
		else {
			final Display display = new Display();
			final Shell shell = app.createShell(display);
			shell.pack();
			shell.setMinimumSize(shell.getSize());
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			display.dispose();
		}
	}

	private Shell createShell(final Display display) {
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

		final Text outputText = new Text(shell, SWT.READ_ONLY|  SWT.BORDER | SWT.WRAP | SWT.MULTI );
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
		final Label codecLabel = new Label(shell, SWT.NONE);
		codecLabel.setText("Algorithm:");
		gridData = new GridData();
		codecLabel.setLayoutData(gridData);

		final Combo codecCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		codecCombo.setItems(CodecType.getNames());

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
		codecCombo.addSelectionListener(new CodecComboSelectionListener(engine, codecCombo, inputText, modeRadios));
		inputText.addModifyListener(new InputTextModifyListener(engine, inputText, outputText));

		return shell;
	}

}
