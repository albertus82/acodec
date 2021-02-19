package it.albertus.acodec.engine;

import it.albertus.acodec.resources.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CodecMode {

	ENCODE('e', "msg.help.encode", "lbl.mode.encode"),
	DECODE('d', "msg.help.decode", "lbl.mode.decode");

	@Getter
	private final char abbreviation;

	private final String consoleLabelKey;
	private final String guiLabelKey;

	public String getLabelForConsole() {
		return Messages.get(consoleLabelKey);
	}

	public String getLabelForGui() {
		return Messages.get(guiLabelKey);
	}

}
