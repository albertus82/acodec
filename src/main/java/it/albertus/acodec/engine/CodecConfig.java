package it.albertus.acodec.engine;

import java.nio.charset.Charset;

import lombok.NonNull;
import lombok.Value;

@Value
public class CodecConfig {

	@NonNull
	CodecMode mode;

	@NonNull
	CodecAlgorithm algorithm;

	@NonNull
	Charset charset;

}
