package it.albertus.codec.gui;

import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
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
import it.albertus.codec.gui.listener.ProcessFileButtonSelectionListener;
import it.albertus.codec.gui.listener.ShellDropListener;
import it.albertus.codec.gui.listener.TextKeyListener;
import it.albertus.codec.resources.Messages;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.util.Version;
import it.albertus.util.logging.LoggerFactory;

public class CodecGui extends Codec implements IShellProvider {

	private static final Logger logger = LoggerFactory.getLogger(CodecGui.class);

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
	private final DropTarget shellDropTarget;

	private boolean dirty = false;

	public CodecGui(final Display display) {
		shell = new Shell(display);
		shell.setImages(Images.getMainIcons());
		shell.setText(Messages.get("msg.application.name"));
		shell.setLayout(new GridLayout(5, false));

		final AboutListener aboutListener = new AboutListener(this);
		if (Util.isCocoa()) {
			try {
				new CocoaUIEnhancer(display).hookApplicationMenu(new CloseListener(this), aboutListener, null);
			}
			catch (final CocoaEnhancerException cee) {
				logger.log(Level.WARNING, Messages.get("err.cocoa.enhancer"), cee);
			}
		}

		/* Input text */
		final Label inputLabel = new Label(shell, SWT.NONE);
		inputLabel.setText(Messages.get("lbl.input"));
		inputLabel.setLayoutData(new GridData());

		inputText = createInputText();

		/* Output text */
		final Label outputLabel = new Label(shell, SWT.NONE);
		outputLabel.setText(Messages.get("lbl.output"));
		outputLabel.setLayoutData(new GridData());

		outputText = createOutputText();

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

		final Composite buttonsComposite = new Composite(shell, SWT.NONE);
		GridDataFactory.swtDefaults().span(1, 2).indent(0, -1).applyTo(buttonsComposite);
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(buttonsComposite);

		// Process file button
		processFileButton = new Button(buttonsComposite, SWT.NONE);
		processFileButton.setEnabled(false);
		processFileButton.setText(Messages.get("lbl.file.process"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(processFileButton);
		processFileButton.addSelectionListener(new ProcessFileButtonSelectionListener(this));

		// About button
		aboutButton = new Button(buttonsComposite, SWT.NONE);
		aboutButton.setText(Messages.get("lbl.about"));
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(aboutButton);
		aboutButton.addSelectionListener(aboutListener);

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

		/* Listener */
		algorithmCombo.addSelectionListener(new AlgorithmComboSelectionListener(this));
		charsetCombo.addSelectionListener(new CharsetComboSelectionListener(this));
		inputText.addModifyListener(new InputTextModifyListener(this));

		/* Drag and drop */
		shellDropTarget = new DropTarget(shell, DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK);
		shellDropTarget.addDropListener(new ShellDropListener(this));

		shell.pack();
		shell.setMinimumSize(shell.getSize());
	}

	public static void start() {
		Display.setAppName(Messages.get("msg.application.name"));
		Display.setAppVersion(Version.getInstance().getNumber());
		final Display display = Display.getDefault();
		final CodecGui gui = new CodecGui(display);
		final Shell shell = gui.getShell();
		shell.addShellListener(new CloseListener(gui));
		shell.open();
		gui.getInputText().notifyListeners(SWT.Modify, null);
		while (!shell.isDisposed()) {
			if (!display.isDisposed() && !display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	private Text createInputText() {
		final Text text = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		final GridData inputTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		if (TEXT_HEIGHT_MULTIPLIER > 1) {
			inputTextGridData.heightHint = text.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
		}
		text.setLayoutData(inputTextGridData);
		text.setTextLimit(TEXT_LIMIT_CHARS);
		text.addKeyListener(new TextKeyListener(text));
		return text;
	}

	private Text createOutputText() {
		final Text text = new Text(shell, SWT.READ_ONLY | SWT.BORDER | SWT.WRAP | SWT.MULTI | SWT.V_SCROLL);
		final GridData outputTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		if (TEXT_HEIGHT_MULTIPLIER > 1) {
			outputTextGridData.heightHint = text.getLineHeight() * TEXT_HEIGHT_MULTIPLIER;
		}
		text.setLayoutData(outputTextGridData);
		if (Util.isWindows()) {
			text.setBackground(inputText.getBackground());
		}
		text.addKeyListener(new TextKeyListener(text));
		return text;
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

	public Map<CodecMode, Button> getModeRadios() {
		return modeRadios;
	}

	public Button getAboutButton() {
		return aboutButton;
	}

	public Button getProcessFileButton() {
		return processFileButton;
	}

	public DropTarget getShellDropTarget() {
		return shellDropTarget;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

}
