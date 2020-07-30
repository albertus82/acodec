package it.albertus.codec.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import it.albertus.util.NewLine;
import it.albertus.util.logging.LoggerFactory;

public class CodecEngineTest {

	private static final Logger log = LoggerFactory.getLogger(CodecEngineTest.class);

	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static final String DIGEST_SEPARATOR = " *";

	private static final String originalString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	private static File originalFile;
	private static final Map<CodecAlgorithm, String> encodedStrings = new EnumMap<>(CodecAlgorithm.class);
	private static final Map<CodecAlgorithm, File> encodedFiles = new EnumMap<>(CodecAlgorithm.class);

	private static CodecEngine engine;

	@BeforeClass
	public static void init() throws IOException {
		createOriginalFile();
		createEncodedStrings();
		createEncodedFiles();

		engine = new CodecEngine();
		engine.setCharset(CHARSET);
	}

	private static void createOriginalFile() throws IOException {
		originalFile = File.createTempFile("original-", ".txt");
		try (final FileWriter fw = new FileWriter(originalFile)) {
			fw.write(originalString);
		}
		log.log(Level.INFO, "Created original file \"{0}\"", originalFile);
	}

	private static void createEncodedStrings() { // @formatter:off
		encodedStrings.put(CodecAlgorithm.BASE16, "4C6F72656D20697073756D20646F6C6F722073697420616D65742C20636F6E73656374657475722061646970697363696E6720656C69742C2073656420646F20656975736D6F642074656D706F7220696E6369646964756E74207574206C61626F726520657420646F6C6F7265206D61676E6120616C697175612E20557420656E696D206164206D696E696D2076656E69616D2C2071756973206E6F737472756420657865726369746174696F6E20756C6C616D636F206C61626F726973206E69736920757420616C697175697020657820656120636F6D6D6F646F20636F6E7365717561742E2044756973206175746520697275726520646F6C6F7220696E20726570726568656E646572697420696E20766F6C7570746174652076656C697420657373652063696C6C756D20646F6C6F726520657520667567696174206E756C6C612070617269617475722E204578636570746575722073696E74206F6363616563617420637570696461746174206E6F6E2070726F6964656E742C2073756E7420696E2063756C706120717569206F666669636961206465736572756E74206D6F6C6C697420616E696D20696420657374206C61626F72756D2E");
		encodedStrings.put(CodecAlgorithm.BASE32, "JRXXEZLNEBUXA43VNUQGI33MN5ZCA43JOQQGC3LFOQWCAY3PNZZWKY3UMV2HK4RAMFSGS4DJONRWS3THEBSWY2LUFQQHGZLEEBSG6IDFNF2XG3LPMQQHIZLNOBXXEIDJNZRWSZDJMR2W45BAOV2CA3DBMJXXEZJAMV2CAZDPNRXXEZJANVQWO3TBEBQWY2LROVQS4ICVOQQGK3TJNUQGCZBANVUW42LNEB3GK3TJMFWSYIDROVUXGIDON5ZXI4TVMQQGK6DFOJRWS5DBORUW63RAOVWGYYLNMNXSA3DBMJXXE2LTEBXGS43JEB2XIIDBNRUXC5LJOAQGK6BAMVQSAY3PNVWW6ZDPEBRW63TTMVYXKYLUFYQEI5LJOMQGC5LUMUQGS4TVOJSSAZDPNRXXEIDJNYQHEZLQOJSWQZLOMRSXE2LUEBUW4IDWN5WHK4DUMF2GKIDWMVWGS5BAMVZXGZJAMNUWY3DVNUQGI33MN5ZGKIDFOUQGM5LHNFQXIIDOOVWGYYJAOBQXE2LBOR2XELRAIV4GGZLQORSXK4RAONUW45BAN5RWGYLFMNQXIIDDOVYGSZDBORQXIIDON5XCA4DSN5UWIZLOOQWCA43VNZ2CA2LOEBRXK3DQMEQHC5LJEBXWMZTJMNUWCIDEMVZWK4TVNZ2CA3LPNRWGS5BAMFXGS3JANFSCAZLTOQQGYYLCN5ZHK3JO");
		encodedStrings.put(CodecAlgorithm.BASE64, "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdCwgc2VkIGRvIGVpdXNtb2QgdGVtcG9yIGluY2lkaWR1bnQgdXQgbGFib3JlIGV0IGRvbG9yZSBtYWduYSBhbGlxdWEuIFV0IGVuaW0gYWQgbWluaW0gdmVuaWFtLCBxdWlzIG5vc3RydWQgZXhlcmNpdGF0aW9uIHVsbGFtY28gbGFib3JpcyBuaXNpIHV0IGFsaXF1aXAgZXggZWEgY29tbW9kbyBjb25zZXF1YXQuIER1aXMgYXV0ZSBpcnVyZSBkb2xvciBpbiByZXByZWhlbmRlcml0IGluIHZvbHVwdGF0ZSB2ZWxpdCBlc3NlIGNpbGx1bSBkb2xvcmUgZXUgZnVnaWF0IG51bGxhIHBhcmlhdHVyLiBFeGNlcHRldXIgc2ludCBvY2NhZWNhdCBjdXBpZGF0YXQgbm9uIHByb2lkZW50LCBzdW50IGluIGN1bHBhIHF1aSBvZmZpY2lhIGRlc2VydW50IG1vbGxpdCBhbmltIGlkIGVzdCBsYWJvcnVtLg==");
		encodedStrings.put(CodecAlgorithm.ASCII85, "9Q+r_D'3P3F*2=BA8c:&EZfF;F<G\"/ATTIG@rH7+ARfgnFEMUH@:X(kBldcuDJ()'Ch[uB+EM+)+CoC5ASH:.D/Wr-FCf<.DfQt7DI[BkBk2@(F<G^J+DbIqDfTD3ATT&*Des?4AKYhuB5V-#@;KXtF^ZmF<HK?pDJ<r1@:UKtBl7X%+Eh=6Bjkj0+E;O<F!,@=F*)GFA0>H.ATD9pFCB9*Df-\\?Ci!Ns@rEK+@:F.qBlbD7Bldu2F`\\a7Ch[m3BlG2+GT^R++Cf>,D/Ws'+Cf>-F(K?6@<=+E7!33b+CTD7AKY]-F`M%9A8c:&EZf(6+ED%4Eb/oqDId=!BlkJ3DBO+@Cis]=@<?''G%GK(F<G.9F(HJ(Bl%U.D'3A-Ci=?*+D#[<Ap%a#@<<W0F_kl&+E1b0Bjl++E\\8J'G[k<(FCfT8+EM77F<GL3@prqY@<<W%F`;&*@<>q\"+Du+8+E2@>Bk1dmF=\\PUF`):DBl5&'F_l#*+E;O<+E(k(Bk(jc+Co&)ATDp2F<GF=Ci!g-+CT/%D'3P'+D#V9+DbIqDfTu;/c~>");
		encodedStrings.put(CodecAlgorithm.BASE91, "Drzg`<fz+$Q;/ETj~i/2:WP1qU2uG9_ou\"L^;meP(Ig,!eLU2u8Pwn32Wf7=YC,RY6LycLeP;Im+oC.!L;e9QnJB%g`<>{KU=53mFlmaef!=yC2U7tAQ2i>z^IL,yC4!t!kb9j_kH<6=MCHRY6W9Yiw\")wlLto+f/W6Y$yY6JQrmW!_1N:Z;/HJ*lQ1o(nc=[[6e8RU0y9Uola(g=[${lT&:+xXi6J.JE<jN1Tb6GF&m32YJi]yCHRY6W9HlVB[I<W=H$y7&G9Qnbrh+e>bT=!P;axeQ3L:gP[eG6U!09MVo_z^Im+|*1T6tM.kLdP[2f,melT20ZQ`o9Z$JH^|*6U*/ax9jW!^IN:yC.!h.pE>i^iDg~<TX1T$&a9jLR8>vz]]0LR7tJ9MmAE=Cw)N1LRR;x.cLZ)WfE?!e6U6tcQ;mZBa=[*Yes!9/QjZpyaef=[6Y$F%&=EnnprYJc,_Y:H^,9.`o9Z$J%*I++$xtV9HlyqB2L::0LR:yTm9je8$J;W.{FTY6KFOiwa4J`/SC.!+/nuXiZrzg~<BB");
		encodedStrings.put(CodecAlgorithm.CRC16, "a8e2");
		encodedStrings.put(CodecAlgorithm.CRC32, "98b2c5bd");
		encodedStrings.put(CodecAlgorithm.ADLER32, "a05ca509");
		encodedStrings.put(CodecAlgorithm.MD2, "4b2ffc802c256a38fd6ccb575cccc27c");
		encodedStrings.put(CodecAlgorithm.MD4, "8db2ba4980fa7d57725e42782ab47b42");
		encodedStrings.put(CodecAlgorithm.MD5, "db89bb5ceab87f9c0fcc2ab36c189c2c");
		encodedStrings.put(CodecAlgorithm.SHA_1, "cd36b370758a259b34845084a6cc38473cb95e27");
		encodedStrings.put(CodecAlgorithm.SHA_224, "b2d9d497bcc3e5be0ca67f08c86087a51322ae48b220ed9241cad7a5");
		encodedStrings.put(CodecAlgorithm.SHA_256, "2d8c2f6d978ca21712b5f6de36c9d31fa8e96a4fa5d8ff8b0188dfb9e7c171bb");
		encodedStrings.put(CodecAlgorithm.SHA_384, "d3b5710e17da84216f1bf08079bbbbf45303baefc6ecd677910a1c33c86cb164281f0f2dcab55bbadc5e8606bdbc16b6");
		encodedStrings.put(CodecAlgorithm.SHA_512, "8ba760cac29cb2b2ce66858ead169174057aa1298ccd581514e6db6dee3285280ee6e3a54c9319071dc8165ff061d77783100d449c937ff1fb4cd1bb516a69b9");
		encodedStrings.put(CodecAlgorithm.SHA_512_224, "6183ece65d9b205ae5a5da08ab39200584e4e893e65b75bf3f7503f5");
		encodedStrings.put(CodecAlgorithm.SHA_512_256, "06ae84e5d26e5537ee9b7b732fb2c091f72884b920102e5ecf3f2b13a6dd1933");
		encodedStrings.put(CodecAlgorithm.SHA3_224, "06774e56a376c3de431f20d1760c289ec07ce8a420e4c9b1c08cbc16");
		encodedStrings.put(CodecAlgorithm.SHA3_256, "bde3f269175e1dcda13848278aa6046bd643cea85b84c8b8bb80952e70b6eae0");
		encodedStrings.put(CodecAlgorithm.SHA3_384, "e297fd85a77fe4f0005785b830dc8e872fb3b5f3349c0181e4d0e4c5ad677512497d5cfe08e753bee70626ba96a47d35");
		encodedStrings.put(CodecAlgorithm.SHA3_512, "f32a9423551351df0a07c0b8c20eb972367c398d61066038e16986448ebfbc3d15ede0ed3693e3905e9a8c601d9d002a06853b9797ef9ab10cbde1009c7d0f09");
		encodedStrings.put(CodecAlgorithm.RIPEMD_128, "540588e8b9d262d70daf728c6753afbd");
		encodedStrings.put(CodecAlgorithm.RIPEMD_160, "c4e3cc08809d907e233a24c10056c9951a67ffe2");
		encodedStrings.put(CodecAlgorithm.RIPEMD_256, "1cfb5d43f287a960ba3181f28225609bbddc2c82bef041d0c462a29f522fa583");
		encodedStrings.put(CodecAlgorithm.RIPEMD_320, "82dc8378303b1317a6d491534b928b68aa0c0803c9e996aadedeb0b97bbecaf118064ca2eb7191b6");
		encodedStrings.put(CodecAlgorithm.TIGER, "761a2fc1c5ab4f8853d71b1a6d1443fd7b5734d378da37c8");
		encodedStrings.put(CodecAlgorithm.WHIRLPOOL, "61d98a2b91d48ae02385aafcf568da1ba973cd401aee273cfd4a4837b4cfbe61adaa6c87c246e4ab83f8f4bb798ce57830c05c0917e7990cbb1fae14ed84b07b");
	} // @formatter:on

	private static void createEncodedFiles() throws IOException {
		for (final Entry<CodecAlgorithm, String> entry : encodedStrings.entrySet()) {
			final File encodedFile = File.createTempFile("encoded-", ".txt." + entry.getKey().getFileExtension());
			try (final Writer fw = Files.newBufferedWriter(encodedFile.toPath())) {
				fw.write(entry.getValue());
			}
			log.log(Level.INFO, "Created temporary encoded file \"{0}\"", encodedFile);
			encodedFiles.put(entry.getKey(), encodedFile);
		}
	}

	@Test
	public void testStringEncoder() {
		engine.setMode(CodecMode.ENCODE);
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getModes().contains(CodecMode.ENCODE)) {
				engine.setAlgorithm(ca);
				log.log(Level.INFO, "Testing string encoding {0}", ca);
				Assert.assertEquals(ca.toString(), encodedStrings.get(ca), engine.run(originalString));
			}
		}
	}

	@Test
	public void testStringDecoder() {
		engine.setMode(CodecMode.DECODE);
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getModes().contains(CodecMode.DECODE)) {
				engine.setAlgorithm(ca);
				log.log(Level.INFO, "Testing string decoding {0}", ca);
				Assert.assertEquals(ca.toString(), originalString, engine.run(encodedStrings.get(ca)));
			}
		}
	}

	@Test
	public void testFileEncoder() throws IOException {
		engine.setMode(CodecMode.ENCODE);
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getModes().contains(CodecMode.ENCODE)) {
				engine.setAlgorithm(ca);
				log.log(Level.INFO, "Testing file encoding {0}", ca);
				final String expected;
				if (!AlgorithmType.ENCODING.equals(ca.getType())) {
					if (CodecAlgorithm.CRC32.equals(ca)) {
						expected = originalFile.getName() + ' ' + encodedStrings.get(ca);
					}
					else {
						expected = encodedStrings.get(ca) + DIGEST_SEPARATOR + originalFile.getName();
					}
				}
				else {
					expected = encodedStrings.get(ca);
				}
				Assert.assertEquals(ca.toString(), expected, testFileEncoder(ca));
			}
		}
	}

	@Test
	public void testFileDecoder() throws IOException {
		engine.setMode(CodecMode.DECODE);
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getModes().contains(CodecMode.DECODE)) {
				engine.setAlgorithm(ca);
				log.log(Level.INFO, "Testing file decoding {0}", ca);
				Assert.assertEquals(ca.toString(), originalString, testFileDecoder(encodedFiles.get(ca)));
			}
		}
	}

	private String testFileEncoder(final CodecAlgorithm ca) throws IOException {
		final File outputFile = File.createTempFile(CodecMode.ENCODE.name().toLowerCase() + '-', '.' + ca.getFileExtension());
		log.log(Level.INFO, "Created temporary encoded file \"{0}\"", outputFile);
		final String value = new ProcessFileTask(engine, originalFile, outputFile).run(() -> false);
		if (!AlgorithmType.ENCODING.equals(ca.getType())) {
			Assert.assertNotNull(value);
			Assert.assertFalse(value.isEmpty());
		}
		else {
			Assert.assertNull(value);
		}
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final InputStream fis = Files.newInputStream(outputFile.toPath())) {
			IOUtils.copy(fis, baos);
		}
		finally {
			try {
				if (Files.deleteIfExists(outputFile.toPath())) {
					log.log(Level.INFO, "Deleted temporary encoded file \"{0}\"", outputFile);
				}
			}
			catch (final IOException e) {
				log.log(Level.SEVERE, e, () -> "Cannot delete temporary encoded file \"" + outputFile + "\":");
				outputFile.deleteOnExit();
			}
		}
		return baos.toString(CHARSET.name()).replaceAll("[" + NewLine.CRLF.toString() + "]+", "");
	}

	private String testFileDecoder(final File file) throws IOException {
		final File outputFile = File.createTempFile(CodecMode.DECODE.name().toLowerCase() + '-', ".txt");
		log.log(Level.INFO, "Created temporary decoded file \"{0}\"", outputFile);
		new ProcessFileTask(engine, file, outputFile).run(() -> false);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (final InputStream fis = Files.newInputStream(outputFile.toPath())) {
			IOUtils.copy(fis, baos);
		}
		finally {
			try {
				if (Files.deleteIfExists(outputFile.toPath())) {
					log.log(Level.INFO, "Deleted temporary decoded file \"{0}\"", outputFile);
				}
			}
			catch (final IOException e) {
				log.log(Level.SEVERE, e, () -> "Cannot delete temporary decoded file \"" + outputFile + "\":");
				outputFile.deleteOnExit();
			}
		}
		return baos.toString(CHARSET.name());
	}

	@AfterClass
	public static void destroy() {
		try {
			if (Files.deleteIfExists(originalFile.toPath())) {
				log.log(Level.INFO, "Deleted original file \"{0}\"", originalFile);
			}
		}
		catch (final IOException e) {
			log.log(Level.SEVERE, e, () -> "Cannot delete original file \"" + originalFile + "\":");
			originalFile.deleteOnExit();
		}
		for (final File encodedFile : encodedFiles.values()) {
			try {
				if (Files.deleteIfExists(encodedFile.toPath())) {
					log.log(Level.INFO, "Deleted temporary encoded file \"{0}\"", encodedFile);
				}
			}
			catch (final IOException e) {
				log.log(Level.SEVERE, e, () -> "Cannot delete temporary encoded file \"" + encodedFile + "\":");
				encodedFile.deleteOnExit();
			}
		}
	}

}
