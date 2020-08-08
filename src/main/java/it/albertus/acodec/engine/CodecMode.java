package it.albertus.acodec.engine;

import it.albertus.acodec.resources.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CodecMode {

	ENCODE("lbl.mode.encode", 'e'),
	DECODE("lbl.mode.decode", 'd');

	private final String labelKey;
	@Getter
	private final char abbreviation;

	public String getName() {
		return Messages.get(labelKey);
	}

}
