package it.albertus.codec.gui;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Text;

public class InputTextModifyListener implements ModifyListener {
	
	private Text input;
	private Text output;
	
	public InputTextModifyListener(Text input, Text output) {
		super();
		this.input = input;
		this.output = output;
	}

	@Override
	public void modifyText(ModifyEvent e) {
		// TODO Auto-generated method stub

	}

}
