package it.albertus.codec;

import it.albertus.codec.gui.GuiImages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Codec {

	public enum CodecOption {
		BASE64("Base64");

		private String name;

		private CodecOption(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
		
		public static String[] getAll() {
			String[] names = new String[CodecOption.values().length];
			for (int i = 0; i < CodecOption.values().length; i++) {
				names[i] = CodecOption.values()[i].name;
			}
			return names;
		}
		
	}

	public static void main(String[] args) {
		Codec codec = new Codec();
		if (args.length > 0) {
			// TODO Console version
		}
		else {
			final Display display = new Display();
			final Shell shell = codec.createShell(display);
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
			display.dispose();
		}
	}

	private Shell createShell(Display display) {
		final Shell shell = new Shell(display);
		shell.setImages(GuiImages.ICONS);
		shell.setText("Codec");
		shell.setSize(500, 150);
		shell.setLayout(new GridLayout(2, false));

		final Label inputLabel = new Label(shell, SWT.NONE);
		inputLabel.setText("Input:");

		final Text inputText = new Text(shell, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		inputText.setLayoutData(gridData);

		final Label outputLabel = new Label(shell, SWT.NONE);
		outputLabel.setText("Output:");
		gridData = new GridData();
		outputLabel.setLayoutData(gridData);

		final Text outputText = new Text(shell, SWT.BORDER | SWT.WRAP | SWT.MULTI);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		outputText.setLayoutData(gridData);

		final Label codecLabel = new Label(shell, SWT.NONE);
		codecLabel.setText("Codec:");
		gridData = new GridData();
		codecLabel.setLayoutData(gridData);

		final Combo codecCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		codecCombo.setItems(CodecOption.getAll());

		shell.open();
		return shell;
	}

}
