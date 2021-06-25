package it.albertus.acodec.common.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import lombok.extern.java.Log;

@Log
class Base45Test {

	private static final Map<String, String> map = new TreeMap<>();

	@BeforeAll
	static void beforeAll() {
		map.put("AB", "BB8");
		map.put("Hello!!", "%69 VD92EX0");
		map.put("base-45", "UJCLQE7W581");
		map.put("ietf!", "QED8WEX0");
		log.log(Level.INFO, "{0}", map);
	}

	@Test
	void testEncodeStream() throws IOException {
		for (final Entry<String, String> e : map.entrySet()) {
			log.info(() -> e.getKey() + " -> " + e.getValue());
			try (final ByteArrayOutputStream os1 = new ByteArrayOutputStream()) {
				try (final Base45OutputStream os2 = new Base45OutputStream(os1)) {
					os2.write(e.getKey().getBytes(StandardCharsets.UTF_8));
				}
				Assertions.assertEquals(e.getValue(), os1.toString(StandardCharsets.UTF_8.name()).replace("\r", "").replace("\n", ""));
			}
		}
	}

	@Test
	void testDecodeStream() throws IOException {
		for (final Entry<String, String> e : map.entrySet()) {
			log.info(() -> e.getValue() + " -> " + e.getKey());
			try (final ByteArrayInputStream is1 = new ByteArrayInputStream(e.getValue().getBytes(StandardCharsets.UTF_8))) {
				final byte[] decodedBytes;
				try (final Base45InputStream is2 = new Base45InputStream(is1)) {
					decodedBytes = IOUtils.readFully(is2, e.getKey().length());
				}
				Assertions.assertEquals(e.getKey(), new String(decodedBytes, StandardCharsets.UTF_8));
			}
		}
	}

	@Test
	void testEncodeString() throws EncoderException, DecoderException {
		final StringCodec sc = new StringCodec(new CodecConfig(CodecMode.ENCODE, CodecAlgorithm.BASE45, StandardCharsets.UTF_8));
		for (final Entry<String, String> e : map.entrySet()) {
			log.info(() -> e.getKey() + " -> " + e.getValue());
			Assertions.assertEquals(e.getValue(), sc.run(e.getKey()));
		}
	}

	@Test
	void testDecodeString() throws EncoderException, DecoderException {
		final StringCodec sc = new StringCodec(new CodecConfig(CodecMode.DECODE, CodecAlgorithm.BASE45, StandardCharsets.UTF_8));
		for (final Entry<String, String> e : map.entrySet()) {
			log.info(() -> e.getValue() + " -> " + e.getKey());
			Assertions.assertEquals(e.getKey(), sc.run(e.getValue()));
		}
	}

}
