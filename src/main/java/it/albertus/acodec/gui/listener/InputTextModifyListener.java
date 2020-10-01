package it.albertus.acodec.gui.listener;

import java.util.logging.Level;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;

import it.albertus.acodec.engine.CodecConfig;
import it.albertus.acodec.engine.StringCodec;
import it.albertus.acodec.gui.CodecGui;
import it.albertus.acodec.resources.Messages;
import lombok.extern.java.Log;

@Log
public class InputTextModifyListener implements ModifyListener {

	private static final String ERROR_SUFFIX = " --";
	private static final String ERROR_PREFIX = "-- ";

	private final CodecGui gui;

	public InputTextModifyListener(final CodecGui gui) {
		this.gui = gui;
	}

	@Override
	public void modifyText(final ModifyEvent event) {
		String result;
		if (gui.isDirty()) {
			gui.setDirty(false);
			gui.getInputText().setText("");
		}

		// Preliminary checks
		if (gui.getAlgorithm() == null) {
			print(Messages.get("msg.missing.algorithm.banner"), true);
			return;
		}
		if (gui.getInputText().getText().isEmpty()) {
			print(Messages.get("msg.missing.input.banner"), true);
			return;
		}

		// Process
		final CodecConfig codecConfig = new CodecConfig(gui.getMode(), gui.getAlgorithm(), gui.getCharset());
		try {
			result = new StringCodec(codecConfig).run(gui.getInputText().getText());
		}
		catch (final EncoderException e) {
			print(Messages.get("err.cannot.encode.banner", codecConfig.getAlgorithm().getName()), true);
			log.log(Level.INFO, Messages.get("err.cannot.encode", codecConfig.getAlgorithm().getName()), e);
			return;
		}
		catch (final DecoderException e) {
			print(Messages.get("err.cannot.decode.banner", codecConfig.getAlgorithm().getName()), true);
			log.log(Level.INFO, Messages.get("err.cannot.decode", codecConfig.getAlgorithm().getName()), e);
			return;
		}
		catch (final Exception e) {
			print(Messages.get("err.unexpected.error.banner"), true);
			log.log(Level.SEVERE, Messages.get("err.unexpected.error"), e);
			return;
		}
		print(result, false);
	}

	private void print(final String text, final boolean error) {
		String outputText = text != null ? text : "";
		if (error) {
			outputText = new StringBuilder(outputText).insert(0, ERROR_PREFIX).append(ERROR_SUFFIX).toString();
			final Color inactiveTextColor = gui.getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
			if (!gui.getOutputText().getForeground().equals(inactiveTextColor)) {
				gui.getOutputText().setForeground(inactiveTextColor);
			}
		}
		else {
			final Color defaultTextColor = gui.getOutputText().getForeground(); // Fix color issues on some Linux GUIs
			if (!gui.getOutputText().getForeground().equals(defaultTextColor)) {
				gui.getOutputText().setForeground(defaultTextColor);
			}
		}
		gui.getOutputText().setText(outputText);
	}

}
