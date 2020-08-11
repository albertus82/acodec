package it.albertus.acodec.engine;

import static it.albertus.acodec.engine.CodecMode.ENCODE;
import static java.nio.charset.Charset.defaultCharset;

import java.nio.charset.Charset;

import lombok.Data;

@Data
public class CodecConfig {

	private CodecMode mode = ENCODE;
	private CodecAlgorithm algorithm;
	private Charset charset = defaultCharset();

}