package com.github.albertus82.acodec.common.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.freehep.util.io.Ascii85InputStream;
import org.freehep.util.io.Ascii85OutputStream;

import it.albertus.util.NewLine;

enum Ascii85 implements BaseNCodec {

	INSTANCE;

	static Ascii85 getCodec() {
		return INSTANCE;
	}

	@Override
	public String encode(final byte[] byteArray) throws IOException {
		final OutputStream baos = new ByteArrayOutputStream();
		try (final InputStream bais = new ByteArrayInputStream(byteArray); final OutputStream a85os = new Ascii85OutputStream(baos)) {
			IOUtils.copy(bais, a85os);
		}
		return baos.toString().replace(NewLine.CR.toString(), "").replace(NewLine.LF.toString(), "");
	}

	@Override
	public byte[] decode(final String encoded) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final InputStream bais = new ByteArrayInputStream(encoded.getBytes(StandardCharsets.US_ASCII)); final InputStream a85is = new Ascii85InputStream(bais)) {
			IOUtils.copy(a85is, baos);
		}
		return baos.toByteArray();
	}

}
