package it.albertus.acodec.engine;

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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import it.albertus.acodec.common.engine.Base45InputStream;
import it.albertus.acodec.common.engine.Base45OutputStream;
import it.albertus.acodec.common.engine.CodecAlgorithm;
import it.albertus.acodec.common.engine.CodecConfig;
import it.albertus.acodec.common.engine.CodecMode;
import it.albertus.acodec.common.engine.StringCodec;
import lombok.extern.java.Log;

@Log
public class Base45Test {

	private static final Map<String, String> map = new TreeMap<>();

	@BeforeClass
	public static void beforeAll() {
		map.put("AB", "BB8");
		map.put("Hello!!", "%69 VD92EX0");
		map.put("base-45", "UJCLQE7W581");
		map.put("ietf!", "QED8WEX0");
		log.log(Level.INFO, "{0}", map);
	}

	@Test
	public void testEncodeStream() throws IOException {
		for (final Entry<String, String> e : map.entrySet()) {
			log.info(() -> e.getKey() + " -> " + e.getValue());
			try (final ByteArrayOutputStream os1 = new ByteArrayOutputStream()) {
				try (final Base45OutputStream os2 = new Base45OutputStream(os1)) {
					os2.write(e.getKey().getBytes(StandardCharsets.UTF_8));
				}
				Assert.assertEquals(e.getValue(), os1.toString(StandardCharsets.UTF_8.name()));
			}
		}
	}

	@Test
	public void testDecodeStream() throws IOException {
		for (final Entry<String, String> e : map.entrySet()) {
			log.info(() -> e.getValue() + " -> " + e.getKey());
			try (final ByteArrayInputStream is1 = new ByteArrayInputStream(e.getValue().getBytes(StandardCharsets.UTF_8))) {
				final byte[] decodedBytes;
				try (final Base45InputStream is2 = new Base45InputStream(is1)) {
					decodedBytes = IOUtils.readFully(is2, e.getKey().length());
				}
				Assert.assertEquals(e.getKey(), new String(decodedBytes, StandardCharsets.UTF_8));
			}
		}
	}

	@Test
	public void testEncodeString() throws EncoderException, DecoderException {
		final StringCodec sc = new StringCodec(new CodecConfig(CodecMode.ENCODE, CodecAlgorithm.BASE45, StandardCharsets.UTF_8));
		for (final Entry<String, String> e : map.entrySet()) {
			log.info(() -> e.getKey() + " -> " + e.getValue());
			Assert.assertEquals(e.getValue(), sc.run(e.getKey()));
		}
	}

	@Test
	public void testDecodeString() throws EncoderException, DecoderException {
		final StringCodec sc = new StringCodec(new CodecConfig(CodecMode.DECODE, CodecAlgorithm.BASE45, StandardCharsets.UTF_8));
		for (final Entry<String, String> e : map.entrySet()) {
			log.info(() -> e.getValue() + " -> " + e.getKey());
			Assert.assertEquals(e.getKey(), sc.run(e.getValue()));
		}
	}

}
