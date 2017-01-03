package it.albertus.codec.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import it.albertus.util.NewLine;

public class CodecEngineTest {

	private static final String CHARSET = "UTF-8";
	private static final String DIGEST_SEPARATOR = " *";

	private static final String originalString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	private static File originalFile;
	private static final Map<CodecAlgorithm, String> encodedStrings = new EnumMap<CodecAlgorithm, String>(CodecAlgorithm.class);
	private static final Map<CodecAlgorithm, File> encodedFiles = new EnumMap<CodecAlgorithm, File>(CodecAlgorithm.class);

	private static CodecEngine engine;

	@BeforeClass
	public static void init() throws IOException {
		createOriginalFile();
		createEncodedStrings();
		createEncodedFiles();

		engine = new CodecEngine();
		engine.setCharset(Charset.forName(CHARSET));
	}

	private static void createOriginalFile() throws IOException {
		FileWriter fw = null;
		try {
			originalFile = File.createTempFile("original-", ".txt");
			fw = new FileWriter(originalFile);
			fw.write(originalString);
			System.out.println("Created original file \"" + originalFile + '"');
		}
		finally {
			IOUtils.closeQuietly(fw);
		}
	}

	private static void createEncodedStrings() {
		encodedStrings.put(CodecAlgorithm.BASE16, "4C6F72656D20697073756D20646F6C6F722073697420616D65742C20636F6E73656374657475722061646970697363696E6720656C69742C2073656420646F20656975736D6F642074656D706F7220696E6369646964756E74207574206C61626F726520657420646F6C6F7265206D61676E6120616C697175612E20557420656E696D206164206D696E696D2076656E69616D2C2071756973206E6F737472756420657865726369746174696F6E20756C6C616D636F206C61626F726973206E69736920757420616C697175697020657820656120636F6D6D6F646F20636F6E7365717561742E2044756973206175746520697275726520646F6C6F7220696E20726570726568656E646572697420696E20766F6C7570746174652076656C697420657373652063696C6C756D20646F6C6F726520657520667567696174206E756C6C612070617269617475722E204578636570746575722073696E74206F6363616563617420637570696461746174206E6F6E2070726F6964656E742C2073756E7420696E2063756C706120717569206F666669636961206465736572756E74206D6F6C6C697420616E696D20696420657374206C61626F72756D2E");
		encodedStrings.put(CodecAlgorithm.BASE32, "JRXXEZLNEBUXA43VNUQGI33MN5ZCA43JOQQGC3LFOQWCAY3PNZZWKY3UMV2HK4RAMFSGS4DJONRWS3THEBSWY2LUFQQHGZLEEBSG6IDFNF2XG3LPMQQHIZLNOBXXEIDJNZRWSZDJMR2W45BAOV2CA3DBMJXXEZJAMV2CAZDPNRXXEZJANVQWO3TBEBQWY2LROVQS4ICVOQQGK3TJNUQGCZBANVUW42LNEB3GK3TJMFWSYIDROVUXGIDON5ZXI4TVMQQGK6DFOJRWS5DBORUW63RAOVWGYYLNMNXSA3DBMJXXE2LTEBXGS43JEB2XIIDBNRUXC5LJOAQGK6BAMVQSAY3PNVWW6ZDPEBRW63TTMVYXKYLUFYQEI5LJOMQGC5LUMUQGS4TVOJSSAZDPNRXXEIDJNYQHEZLQOJSWQZLOMRSXE2LUEBUW4IDWN5WHK4DUMF2GKIDWMVWGS5BAMVZXGZJAMNUWY3DVNUQGI33MN5ZGKIDFOUQGM5LHNFQXIIDOOVWGYYJAOBQXE2LBOR2XELRAIV4GGZLQORSXK4RAONUW45BAN5RWGYLFMNQXIIDDOVYGSZDBORQXIIDON5XCA4DSN5UWIZLOOQWCA43VNZ2CA2LOEBRXK3DQMEQHC5LJEBXWMZTJMNUWCIDEMVZWK4TVNZ2CA3LPNRWGS5BAMFXGS3JANFSCAZLTOQQGYYLCN5ZHK3JO");
		encodedStrings.put(CodecAlgorithm.BASE64, "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdCwgc2VkIGRvIGVpdXNtb2QgdGVtcG9yIGluY2lkaWR1bnQgdXQgbGFib3JlIGV0IGRvbG9yZSBtYWduYSBhbGlxdWEuIFV0IGVuaW0gYWQgbWluaW0gdmVuaWFtLCBxdWlzIG5vc3RydWQgZXhlcmNpdGF0aW9uIHVsbGFtY28gbGFib3JpcyBuaXNpIHV0IGFsaXF1aXAgZXggZWEgY29tbW9kbyBjb25zZXF1YXQuIER1aXMgYXV0ZSBpcnVyZSBkb2xvciBpbiByZXByZWhlbmRlcml0IGluIHZvbHVwdGF0ZSB2ZWxpdCBlc3NlIGNpbGx1bSBkb2xvcmUgZXUgZnVnaWF0IG51bGxhIHBhcmlhdHVyLiBFeGNlcHRldXIgc2ludCBvY2NhZWNhdCBjdXBpZGF0YXQgbm9uIHByb2lkZW50LCBzdW50IGluIGN1bHBhIHF1aSBvZmZpY2lhIGRlc2VydW50IG1vbGxpdCBhbmltIGlkIGVzdCBsYWJvcnVtLg==");
		encodedStrings.put(CodecAlgorithm.ASCII85, "9Q+r_D'3P3F*2=BA8c:&EZfF;F<G\"/ATTIG@rH7+ARfgnFEMUH@:X(kBldcuDJ()'Ch[uB+EM+)+CoC5ASH:.D/Wr-FCf<.DfQt7DI[BkBk2@(F<G^J+DbIqDfTD3ATT&*Des?4AKYhuB5V-#@;KXtF^ZmF<HK?pDJ<r1@:UKtBl7X%+Eh=6Bjkj0+E;O<F!,@=F*)GFA0>H.ATD9pFCB9*Df-\\?Ci!Ns@rEK+@:F.qBlbD7Bldu2F`\\a7Ch[m3BlG2+GT^R++Cf>,D/Ws'+Cf>-F(K?6@<=+E7!33b+CTD7AKY]-F`M%9A8c:&EZf(6+ED%4Eb/oqDId=!BlkJ3DBO+@Cis]=@<?''G%GK(F<G.9F(HJ(Bl%U.D'3A-Ci=?*+D#[<Ap%a#@<<W0F_kl&+E1b0Bjl++E\\8J'G[k<(FCfT8+EM77F<GL3@prqY@<<W%F`;&*@<>q\"+Du+8+E2@>Bk1dmF=\\PUF`):DBl5&'F_l#*+E;O<+E(k(Bk(jc+Co&)ATDp2F<GF=Ci!g-+CT/%D'3P'+D#V9+DbIqDfTu;/c~>");
		encodedStrings.put(CodecAlgorithm.BASE91, "Drzg`<fz+$Q;/ETj~i/2:WP1qU2uG9_ou\"L^;meP(Ig,!eLU2u8Pwn32Wf7=YC,RY6LycLeP;Im+oC.!L;e9QnJB%g`<>{KU=53mFlmaef!=yC2U7tAQ2i>z^IL,yC4!t!kb9j_kH<6=MCHRY6W9Yiw\")wlLto+f/W6Y$yY6JQrmW!_1N:Z;/HJ*lQ1o(nc=[[6e8RU0y9Uola(g=[${lT&:+xXi6J.JE<jN1Tb6GF&m32YJi]yCHRY6W9HlVB[I<W=H$y7&G9Qnbrh+e>bT=!P;axeQ3L:gP[eG6U!09MVo_z^Im+|*1T6tM.kLdP[2f,melT20ZQ`o9Z$JH^|*6U*/ax9jW!^IN:yC.!h.pE>i^iDg~<TX1T$&a9jLR8>vz]]0LR7tJ9MmAE=Cw)N1LRR;x.cLZ)WfE?!e6U6tcQ;mZBa=[*Yes!9/QjZpyaef=[6Y$F%&=EnnprYJc,_Y:H^,9.`o9Z$J%*I++$xtV9HlyqB2L::0LR:yTm9je8$J;W.{FTY6KFOiwa4J`/SC.!+/nuXiZrzg~<BB");
		encodedStrings.put(CodecAlgorithm.MD2, "4b2ffc802c256a38fd6ccb575cccc27c");
		encodedStrings.put(CodecAlgorithm.MD4, "8db2ba4980fa7d57725e42782ab47b42");
		encodedStrings.put(CodecAlgorithm.MD5, "db89bb5ceab87f9c0fcc2ab36c189c2c");
		encodedStrings.put(CodecAlgorithm.SHA1, "cd36b370758a259b34845084a6cc38473cb95e27");
		encodedStrings.put(CodecAlgorithm.SHA256, "2d8c2f6d978ca21712b5f6de36c9d31fa8e96a4fa5d8ff8b0188dfb9e7c171bb");
		encodedStrings.put(CodecAlgorithm.SHA384, "d3b5710e17da84216f1bf08079bbbbf45303baefc6ecd677910a1c33c86cb164281f0f2dcab55bbadc5e8606bdbc16b6");
		encodedStrings.put(CodecAlgorithm.SHA512, "8ba760cac29cb2b2ce66858ead169174057aa1298ccd581514e6db6dee3285280ee6e3a54c9319071dc8165ff061d77783100d449c937ff1fb4cd1bb516a69b9");
	}

	private static void createEncodedFiles() throws IOException {
		for (final Entry<CodecAlgorithm, String> entry : encodedStrings.entrySet()) {
			File encodedFile;
			FileWriter fw = null;
			try {
				encodedFile = File.createTempFile("encoded-", ".txt." + entry.getKey().name().toLowerCase());
				fw = new FileWriter(encodedFile);
				fw.write(entry.getValue());
				System.out.println("Created temporary encoded file \"" + encodedFile + '"');
			}
			finally {
				IOUtils.closeQuietly(fw);
			}
			encodedFiles.put(entry.getKey(), encodedFile);
		}
	}

	@Test
	public void testStringEncoder() {
		engine.setMode(CodecMode.ENCODE);
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getModes().contains(CodecMode.ENCODE)) {
				engine.setAlgorithm(ca);
				System.out.println("Testing string encoding " + ca);
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
				System.out.println("Testing string decoding " + ca);
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
				System.out.println("Testing file encoding " + ca);
				Assert.assertEquals(ca.toString(), encodedStrings.get(ca) + (ca.isDigest() ? DIGEST_SEPARATOR + originalFile.getName() : ""), testFileEncoder(ca));
			}
		}
	}

	@Test
	public void testFileDecoder() throws IOException {
		engine.setMode(CodecMode.DECODE);
		for (final CodecAlgorithm ca : CodecAlgorithm.values()) {
			if (ca.getModes().contains(CodecMode.DECODE)) {
				engine.setAlgorithm(ca);
				System.out.println("Testing file decoding " + ca);
				Assert.assertEquals(ca.toString(), originalString, testFileDecoder(ca, encodedFiles.get(ca)));
			}
		}
	}

	private String testFileEncoder(final CodecAlgorithm ca) throws IOException {
		final File outputFile = File.createTempFile(CodecMode.ENCODE.name().toLowerCase() + '-', '.' + ca.name().toLowerCase());
		System.out.println("Created temporary encoded file \"" + outputFile + '"');
		engine.run(originalFile, outputFile);
		FileInputStream fis = null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			fis = new FileInputStream(outputFile);
			IOUtils.copy(fis, baos);
		}
		finally {
			IOUtils.closeQuietly(baos);
			IOUtils.closeQuietly(fis);
			if (outputFile.delete()) {
				System.out.println("Deleted temporary encoded file \"" + outputFile + '"');
			}
			else {
				System.err.println("Cannot delete temporary encoded file \"" + outputFile + '"');
				outputFile.deleteOnExit();
			}
		}
		return baos.toString(CHARSET).replaceAll("[" + NewLine.CRLF.toString() + "]+", "");
	}

	private String testFileDecoder(final CodecAlgorithm ca, final File file) throws IOException {
		final File outputFile = File.createTempFile(CodecMode.DECODE.name().toLowerCase() + '-', ".txt");
		System.out.println("Created temporary decoded file \"" + outputFile + '"');
		engine.run(file, outputFile);
		FileInputStream fis = null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			fis = new FileInputStream(outputFile);
			IOUtils.copy(fis, baos);
		}
		finally {
			IOUtils.closeQuietly(baos);
			IOUtils.closeQuietly(fis);
			if (outputFile.delete()) {
				System.out.println("Deleted temporary decoded file \"" + outputFile + '"');
			}
			else {
				System.err.println("Cannot delete temporary decoded file \"" + outputFile + '"');
				outputFile.deleteOnExit();
			}
		}
		return baos.toString(CHARSET);
	}

	@AfterClass
	public static void destroy() {
		if (originalFile.delete()) {
			System.out.println("Deleted original file \"" + originalFile + '"');
		}
		else {
			System.err.println("Cannot delete original file \"" + originalFile + '"');
			originalFile.deleteOnExit();
		}
		for (final File encodedFile : encodedFiles.values()) {
			if (encodedFile.delete()) {
				System.out.println("Deleted temporary encoded file \"" + encodedFile + '"');
			}
			else {
				System.err.println("Cannot delete temporary encoded file \"" + encodedFile + '"');
				encodedFile.deleteOnExit();
			}
		}
	}

}
