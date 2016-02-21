package it.albertus.codec.gui;

import org.eclipse.swt.SWT;

public class Utils {

	public static final char KEY_SELECT_ALL = 'a';
	public static final char KEY_COPY = 'c';

	public static String getMod1KeyLabel() {
		return SWT.MOD1 == SWT.COMMAND ? "Cmd" : "Ctrl";
	}

}
