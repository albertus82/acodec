package io.github.albertus82.acodec.common.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CodecMode {

	ENCODE('e'),
	DECODE('d');

	private final char abbreviation;

}
