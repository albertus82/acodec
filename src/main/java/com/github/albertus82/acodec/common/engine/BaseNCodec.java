package com.github.albertus82.acodec.common.engine;

import java.io.IOException;

interface BaseNCodec {

	String encode(byte[] byteArray) throws IOException;

	byte[] decode(String encoded) throws IOException;

}
