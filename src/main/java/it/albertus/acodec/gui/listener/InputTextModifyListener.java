package it.albertus.acodec.gui.listener;

import java.util.logging.Level;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;

import it.albertus.acodec.engine.MissingAlgorithmException;
import it.albertus.acodec.engine.MissingInputException;
import it.albertus.acodec.engine.StringCodec;
import it.albertus.acodec.gui.CodecGui;
import it.albertus.acodec.resources.Messages;
import lombok.extern.java.Log;

@Log
public class InputTextModifyListener implements ModifyListener {

	private static final String ERROR_SUFFIX = " --";
	private static final String ERROR_PREFIX = "-- ";

	private final CodecGui gui;
	private final Color inactiveTextColor;
	private Color defaultTextColor;

	public InputTextModifyListener(final CodecGui gui) {
		this.gui = gui;
		this.inactiveTextColor = gui.getShell().getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
	}

	@Override
	public void modifyText(final ModifyEvent event) {
		String result;
		if (gui.isDirty()) {
			gui.setDirty(false);
			gui.getInputText().setText("");
		}
		try {
			result = new StringCodec(gui.getConfig()).run(gui.getInputText().getText());
		}
		catch (final EncoderException e) {
			print(Messages.get("err.cannot.encode.banner", gui.getConfig().getAlgorithm().getName()), true);
			log.log(Level.INFO, Messages.get("err.cannot.encode", gui.getConfig().getAlgorithm().getName()), e);
			return;
		}
		catch (final DecoderException e) {
			print(Messages.get("err.cannot.decode.banner", gui.getConfig().getAlgorithm().getName()), true);
			log.log(Level.INFO, Messages.get("err.cannot.decode", gui.getConfig().getAlgorithm().getName()), e);
			return;
		}
		catch (final MissingAlgorithmException e) {
			print(Messages.get("msg.missing.algorithm.banner"), true);
			log.log(Level.FINE, Messages.get("msg.missing.algorithm"), e);
			return;
		}
		catch (final MissingInputException e) {
			print(Messages.get("msg.missing.input.banner"), true);
			log.log(Level.FINE, Messages.get("msg.missing.input"), e);
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
		if (defaultTextColor == null) { // Fix color issues on some Linux GUIs
			defaultTextColor = gui.getOutputText().getForeground();
		}
		if (error) {
			outputText = new StringBuilder(outputText).insert(0, ERROR_PREFIX).append(ERROR_SUFFIX).toString();
			if (!gui.getOutputText().getForeground().equals(inactiveTextColor)) {
				gui.getOutputText().setForeground(inactiveTextColor);
			}
		}
		else {
			if (!gui.getOutputText().getForeground().equals(defaultTextColor)) {
				gui.getOutputText().setForeground(defaultTextColor);
			}
		}
		gui.getOutputText().setText(outputText);
	}

}
