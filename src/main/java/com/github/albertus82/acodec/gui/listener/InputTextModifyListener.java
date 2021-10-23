package com.github.albertus82.acodec.gui.listener;

import static com.github.albertus82.acodec.common.engine.AlgorithmType.CHECKSUM;
import static com.github.albertus82.acodec.common.engine.AlgorithmType.HASH;
import static com.github.albertus82.acodec.gui.GuiStatus.DIRTY;
import static com.github.albertus82.acodec.gui.GuiStatus.ERROR;
import static com.github.albertus82.acodec.gui.GuiStatus.OK;
import static com.github.albertus82.acodec.gui.GuiStatus.UNDEFINED;

import java.util.EnumSet;
import java.util.logging.Level;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import com.github.albertus82.acodec.common.engine.CodecConfig;
import com.github.albertus82.acodec.common.engine.StringCodec;
import com.github.albertus82.acodec.common.resources.Messages;
import com.github.albertus82.acodec.gui.CodecGui;
import com.github.albertus82.acodec.gui.resources.GuiMessages;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class InputTextModifyListener implements ModifyListener {

	private static final Messages messages = GuiMessages.INSTANCE;

	@NonNull
	private final CodecGui gui;

	@Override
	public void modifyText(final ModifyEvent event) {
		String result;
		if (DIRTY.equals(gui.getStatus())) {
			gui.setInputText("", UNDEFINED);
			gui.getInputText().setFocus();
		}

		gui.getInputLengthText().setText(Integer.toString(gui.getInputText().getCharCount()));

		// Preliminary checks
		if (gui.getAlgorithm() == null) {
			gui.setOutputText(messages.get("gui.message.missing.algorithm.banner"), ERROR);
			return;
		}
		if (gui.getInputText().getText().isEmpty() && !EnumSet.of(CHECKSUM, HASH).contains(gui.getAlgorithm().getType())) {
			gui.setOutputText(messages.get("gui.message.missing.input.banner"), ERROR);
			return;
		}

		// Process
		final CodecConfig codecConfig = new CodecConfig(gui.getMode(), gui.getAlgorithm(), gui.getCharset());
		try {
			result = new StringCodec(codecConfig).run(gui.getInputText().getText());
		}
		catch (final EncoderException e) {
			gui.setOutputText(messages.get("gui.error.cannot.encode.banner", codecConfig.getAlgorithm().getName()), ERROR);
			log.log(Level.FINE, "Encoding process failure:", e);
			return;
		}
		catch (final DecoderException e) {
			gui.setOutputText(messages.get("gui.error.cannot.decode.banner", codecConfig.getAlgorithm().getName()), ERROR);
			log.log(Level.FINE, "Decoding process failure:", e);
			return;
		}
		catch (final Exception e) {
			gui.setOutputText(messages.get("gui.error.unexpected.banner"), ERROR);
			log.log(Level.SEVERE, "An unexpected error has occurred:", e);
			return;
		}
		gui.setOutputText(result, OK);
	}

}
