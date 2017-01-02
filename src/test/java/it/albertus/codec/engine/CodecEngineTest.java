package it.albertus.codec.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import it.albertus.util.NewLine;

public class CodecEngineTest {

	private static final String CHARSET = "UTF-8";
	private static final String DIGEST_SEPARATOR = " *";

	private static final String ORIGINAL = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

	private static final String BASE16 = "4C6F72656D20697073756D20646F6C6F722073697420616D65742C20636F6E73656374657475722061646970697363696E6720656C69742C2073656420646F20656975736D6F642074656D706F7220696E6369646964756E74207574206C61626F726520657420646F6C6F7265206D61676E6120616C697175612E20557420656E696D206164206D696E696D2076656E69616D2C2071756973206E6F737472756420657865726369746174696F6E20756C6C616D636F206C61626F726973206E69736920757420616C697175697020657820656120636F6D6D6F646F20636F6E7365717561742E2044756973206175746520697275726520646F6C6F7220696E20726570726568656E646572697420696E20766F6C7570746174652076656C697420657373652063696C6C756D20646F6C6F726520657520667567696174206E756C6C612070617269617475722E204578636570746575722073696E74206F6363616563617420637570696461746174206E6F6E2070726F6964656E742C2073756E7420696E2063756C706120717569206F666669636961206465736572756E74206D6F6C6C697420616E696D20696420657374206C61626F72756D2E";
	private static final String BASE32 = "JRXXEZLNEBUXA43VNUQGI33MN5ZCA43JOQQGC3LFOQWCAY3PNZZWKY3UMV2HK4RAMFSGS4DJONRWS3THEBSWY2LUFQQHGZLEEBSG6IDFNF2XG3LPMQQHIZLNOBXXEIDJNZRWSZDJMR2W45BAOV2CA3DBMJXXEZJAMV2CAZDPNRXXEZJANVQWO3TBEBQWY2LROVQS4ICVOQQGK3TJNUQGCZBANVUW42LNEB3GK3TJMFWSYIDROVUXGIDON5ZXI4TVMQQGK6DFOJRWS5DBORUW63RAOVWGYYLNMNXSA3DBMJXXE2LTEBXGS43JEB2XIIDBNRUXC5LJOAQGK6BAMVQSAY3PNVWW6ZDPEBRW63TTMVYXKYLUFYQEI5LJOMQGC5LUMUQGS4TVOJSSAZDPNRXXEIDJNYQHEZLQOJSWQZLOMRSXE2LUEBUW4IDWN5WHK4DUMF2GKIDWMVWGS5BAMVZXGZJAMNUWY3DVNUQGI33MN5ZGKIDFOUQGM5LHNFQXIIDOOVWGYYJAOBQXE2LBOR2XELRAIV4GGZLQORSXK4RAONUW45BAN5RWGYLFMNQXIIDDOVYGSZDBORQXIIDON5XCA4DSN5UWIZLOOQWCA43VNZ2CA2LOEBRXK3DQMEQHC5LJEBXWMZTJMNUWCIDEMVZWK4TVNZ2CA3LPNRWGS5BAMFXGS3JANFSCAZLTOQQGYYLCN5ZHK3JO";
	private static final String BASE64 = "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIGFkaXBpc2NpbmcgZWxpdCwgc2VkIGRvIGVpdXNtb2QgdGVtcG9yIGluY2lkaWR1bnQgdXQgbGFib3JlIGV0IGRvbG9yZSBtYWduYSBhbGlxdWEuIFV0IGVuaW0gYWQgbWluaW0gdmVuaWFtLCBxdWlzIG5vc3RydWQgZXhlcmNpdGF0aW9uIHVsbGFtY28gbGFib3JpcyBuaXNpIHV0IGFsaXF1aXAgZXggZWEgY29tbW9kbyBjb25zZXF1YXQuIER1aXMgYXV0ZSBpcnVyZSBkb2xvciBpbiByZXByZWhlbmRlcml0IGluIHZvbHVwdGF0ZSB2ZWxpdCBlc3NlIGNpbGx1bSBkb2xvcmUgZXUgZnVnaWF0IG51bGxhIHBhcmlhdHVyLiBFeGNlcHRldXIgc2ludCBvY2NhZWNhdCBjdXBpZGF0YXQgbm9uIHByb2lkZW50LCBzdW50IGluIGN1bHBhIHF1aSBvZmZpY2lhIGRlc2VydW50IG1vbGxpdCBhbmltIGlkIGVzdCBsYWJvcnVtLg==";
	private static final String ASCII85 = "9Q+r_D'3P3F*2=BA8c:&EZfF;F<G\"/ATTIG@rH7+ARfgnFEMUH@:X(kBldcuDJ()'Ch[uB+EM+)+CoC5ASH:.D/Wr-FCf<.DfQt7DI[BkBk2@(F<G^J+DbIqDfTD3ATT&*Des?4AKYhuB5V-#@;KXtF^ZmF<HK?pDJ<r1@:UKtBl7X%+Eh=6Bjkj0+E;O<F!,@=F*)GFA0>H.ATD9pFCB9*Df-\\?Ci!Ns@rEK+@:F.qBlbD7Bldu2F`\\a7Ch[m3BlG2+GT^R++Cf>,D/Ws'+Cf>-F(K?6@<=+E7!33b+CTD7AKY]-F`M%9A8c:&EZf(6+ED%4Eb/oqDId=!BlkJ3DBO+@Cis]=@<?''G%GK(F<G.9F(HJ(Bl%U.D'3A-Ci=?*+D#[<Ap%a#@<<W0F_kl&+E1b0Bjl++E\\8J'G[k<(FCfT8+EM77F<GL3@prqY@<<W%F`;&*@<>q\"+Du+8+E2@>Bk1dmF=\\PUF`):DBl5&'F_l#*+E;O<+E(k(Bk(jc+Co&)ATDp2F<GF=Ci!g-+CT/%D'3P'+D#V9+DbIqDfTu;/c~>";
	private static final String BASE91 = "Drzg`<fz+$Q;/ETj~i/2:WP1qU2uG9_ou\"L^;meP(Ig,!eLU2u8Pwn32Wf7=YC,RY6LycLeP;Im+oC.!L;e9QnJB%g`<>{KU=53mFlmaef!=yC2U7tAQ2i>z^IL,yC4!t!kb9j_kH<6=MCHRY6W9Yiw\")wlLto+f/W6Y$yY6JQrmW!_1N:Z;/HJ*lQ1o(nc=[[6e8RU0y9Uola(g=[${lT&:+xXi6J.JE<jN1Tb6GF&m32YJi]yCHRY6W9HlVB[I<W=H$y7&G9Qnbrh+e>bT=!P;axeQ3L:gP[eG6U!09MVo_z^Im+|*1T6tM.kLdP[2f,melT20ZQ`o9Z$JH^|*6U*/ax9jW!^IN:yC.!h.pE>i^iDg~<TX1T$&a9jLR8>vz]]0LR7tJ9MmAE=Cw)N1LRR;x.cLZ)WfE?!e6U6tcQ;mZBa=[*Yes!9/QjZpyaef=[6Y$F%&=EnnprYJc,_Y:H^,9.`o9Z$J%*I++$xtV9HlyqB2L::0LR:yTm9je8$J;W.{FTY6KFOiwa4J`/SC.!+/nuXiZrzg~<BB";
	private static final String MD2 = "4b2ffc802c256a38fd6ccb575cccc27c";
	private static final String MD4 = "8db2ba4980fa7d57725e42782ab47b42";
	private static final String MD5 = "db89bb5ceab87f9c0fcc2ab36c189c2c";
	private static final String SHA1 = "cd36b370758a259b34845084a6cc38473cb95e27";
	private static final String SHA256 = "2d8c2f6d978ca21712b5f6de36c9d31fa8e96a4fa5d8ff8b0188dfb9e7c171bb";
	private static final String SHA384 = "d3b5710e17da84216f1bf08079bbbbf45303baefc6ecd677910a1c33c86cb164281f0f2dcab55bbadc5e8606bdbc16b6";
	private static final String SHA512 = "8ba760cac29cb2b2ce66858ead169174057aa1298ccd581514e6db6dee3285280ee6e3a54c9319071dc8165ff061d77783100d449c937ff1fb4cd1bb516a69b9";

	private static File originalFile;
	private static File base16File;
	private static File base32File;
	private static File base64File;
	private static File ascii85File;
	private static File base91File;
	private static File md2File;
	private static File md4File;
	private static File md5File;
	private static File sha1File;
	private static File sha256File;
	private static File sha384File;
	private static File sha512File;

	private static CodecEngine engine;

	@BeforeClass
	public static void init() throws IOException {
		engine = new CodecEngine();
		engine.setCharset(Charset.forName(CHARSET));

		originalFile = createOriginalFile();

		base16File = createEncodedFile(CodecAlgorithm.BASE16, BASE16);
		base32File = createEncodedFile(CodecAlgorithm.BASE32, BASE32);
		base64File = createEncodedFile(CodecAlgorithm.BASE64, BASE64);
		ascii85File = createEncodedFile(CodecAlgorithm.ASCII85, ASCII85);
		base91File = createEncodedFile(CodecAlgorithm.BASE91, BASE91);
		md2File = createEncodedFile(CodecAlgorithm.MD2, MD2);
		md4File = createEncodedFile(CodecAlgorithm.MD4, MD4);
		md5File = createEncodedFile(CodecAlgorithm.MD5, MD5);
		sha1File = createEncodedFile(CodecAlgorithm.SHA1, SHA1);
		sha256File = createEncodedFile(CodecAlgorithm.SHA256, SHA256);
		sha384File = createEncodedFile(CodecAlgorithm.SHA384, SHA384);
		sha512File = createEncodedFile(CodecAlgorithm.SHA512, SHA512);
	}

	@AfterClass
	public static void destroy() {
		if (!base16File.delete()) {
			originalFile.deleteOnExit();
		}
		if (!base32File.delete()) {
			base32File.deleteOnExit();
		}
		if (!base64File.delete()) {
			base64File.deleteOnExit();
		}
		if (!ascii85File.delete()) {
			ascii85File.deleteOnExit();
		}
		if (!base91File.delete()) {
			base91File.deleteOnExit();
		}
		if (!md2File.delete()) {
			md2File.deleteOnExit();
		}
		if (!md4File.delete()) {
			md4File.deleteOnExit();
		}
		if (!md5File.delete()) {
			md5File.deleteOnExit();
		}
		if (!sha1File.delete()) {
			sha1File.deleteOnExit();
		}
		if (!sha256File.delete()) {
			sha256File.deleteOnExit();
		}
		if (!sha384File.delete()) {
			sha384File.deleteOnExit();
		}
		if (!sha512File.delete()) {
			sha512File.deleteOnExit();
		}
	}

	@Test
	public void testStringEncoder() {
		engine.setMode(CodecMode.ENCODE);
		engine.setAlgorithm(CodecAlgorithm.BASE16);
		Assert.assertEquals(BASE16, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.BASE32);
		Assert.assertEquals(BASE32, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.BASE64);
		Assert.assertEquals(BASE64, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.ASCII85);
		Assert.assertEquals(ASCII85, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.BASE91);
		Assert.assertEquals(BASE91, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.MD2);
		Assert.assertEquals(MD2, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.MD4);
		Assert.assertEquals(MD4, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.MD5);
		Assert.assertEquals(MD5, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.SHA1);
		Assert.assertEquals(SHA1, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.SHA256);
		Assert.assertEquals(SHA256, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.SHA384);
		Assert.assertEquals(SHA384, engine.run(ORIGINAL));
		engine.setAlgorithm(CodecAlgorithm.SHA512);
		Assert.assertEquals(SHA512, engine.run(ORIGINAL));
	}

	@Test
	public void testStringDecoder() {
		engine.setMode(CodecMode.DECODE);
		engine.setAlgorithm(CodecAlgorithm.BASE16);
		Assert.assertEquals(ORIGINAL, engine.run(BASE16));
		engine.setAlgorithm(CodecAlgorithm.BASE32);
		Assert.assertEquals(ORIGINAL, engine.run(BASE32));
		engine.setAlgorithm(CodecAlgorithm.BASE64);
		Assert.assertEquals(ORIGINAL, engine.run(BASE64));
		engine.setAlgorithm(CodecAlgorithm.ASCII85);
		Assert.assertEquals(ORIGINAL, engine.run(ASCII85));
		engine.setAlgorithm(CodecAlgorithm.BASE91);
		Assert.assertEquals(ORIGINAL, engine.run(BASE91));
	}

	@Test
	public void testFileEncoder() throws IOException {
		engine.setAlgorithm(CodecAlgorithm.BASE16);
		Assert.assertEquals(BASE16, testFileEncoder(CodecAlgorithm.BASE16));
		engine.setAlgorithm(CodecAlgorithm.BASE32);
		Assert.assertEquals(BASE32, testFileEncoder(CodecAlgorithm.BASE32));
		engine.setAlgorithm(CodecAlgorithm.BASE64);
		Assert.assertEquals(BASE64, testFileEncoder(CodecAlgorithm.BASE64));
		engine.setAlgorithm(CodecAlgorithm.ASCII85);
		Assert.assertEquals(ASCII85, testFileEncoder(CodecAlgorithm.ASCII85));
		engine.setAlgorithm(CodecAlgorithm.BASE91);
		Assert.assertEquals(BASE91, testFileEncoder(CodecAlgorithm.BASE91));
		engine.setAlgorithm(CodecAlgorithm.MD2);
		Assert.assertEquals(MD2 + DIGEST_SEPARATOR + originalFile.getName(), testFileEncoder(CodecAlgorithm.MD2));
		engine.setAlgorithm(CodecAlgorithm.MD4);
		Assert.assertEquals(MD4 + DIGEST_SEPARATOR + originalFile.getName(), testFileEncoder(CodecAlgorithm.MD4));
		engine.setAlgorithm(CodecAlgorithm.MD5);
		Assert.assertEquals(MD5 + DIGEST_SEPARATOR + originalFile.getName(), testFileEncoder(CodecAlgorithm.MD5));
		engine.setAlgorithm(CodecAlgorithm.SHA1);
		Assert.assertEquals(SHA1 + DIGEST_SEPARATOR + originalFile.getName(), testFileEncoder(CodecAlgorithm.SHA1));
		engine.setAlgorithm(CodecAlgorithm.SHA256);
		Assert.assertEquals(SHA256 + DIGEST_SEPARATOR + originalFile.getName(), testFileEncoder(CodecAlgorithm.SHA256));
		engine.setAlgorithm(CodecAlgorithm.SHA384);
		Assert.assertEquals(SHA384 + DIGEST_SEPARATOR + originalFile.getName(), testFileEncoder(CodecAlgorithm.SHA384));
		engine.setAlgorithm(CodecAlgorithm.SHA512);
		Assert.assertEquals(SHA512 + DIGEST_SEPARATOR + originalFile.getName(), testFileEncoder(CodecAlgorithm.SHA512));
	}

	@Test
	public void testFileDecoder() throws IOException {
		engine.setAlgorithm(CodecAlgorithm.BASE16);
		Assert.assertEquals(ORIGINAL, testFileDecoder(CodecAlgorithm.BASE16, base16File));
		engine.setAlgorithm(CodecAlgorithm.BASE32);
		Assert.assertEquals(ORIGINAL, testFileDecoder(CodecAlgorithm.BASE32, base32File));
		engine.setAlgorithm(CodecAlgorithm.BASE64);
		Assert.assertEquals(ORIGINAL, testFileDecoder(CodecAlgorithm.BASE64, base64File));
		engine.setAlgorithm(CodecAlgorithm.ASCII85);
		Assert.assertEquals(ORIGINAL, testFileDecoder(CodecAlgorithm.ASCII85, ascii85File));
		engine.setAlgorithm(CodecAlgorithm.BASE91);
		Assert.assertEquals(ORIGINAL, testFileDecoder(CodecAlgorithm.BASE91, base91File));
	}

	private static File createOriginalFile() throws IOException {
		File file;
		FileWriter fw = null;
		try {
			file = File.createTempFile(CodecEngineTest.class.getSimpleName(), ".txt");
			fw = new FileWriter(file);
			fw.write(ORIGINAL);
		}
		finally {
			IOUtils.closeQuietly(fw);
		}
		return file;
	}

	private static File createEncodedFile(final CodecAlgorithm ca, final String content) throws IOException {
		File file;
		FileWriter fw = null;
		try {
			file = File.createTempFile(CodecEngineTest.class.getSimpleName(), ".txt." + ca.name().toLowerCase());
			fw = new FileWriter(file);
			fw.write(content);
		}
		finally {
			IOUtils.closeQuietly(fw);
		}
		return file;
	}

	private String testFileEncoder(final CodecAlgorithm ca) throws IOException {
		engine.setMode(CodecMode.ENCODE);
		File outputFile = File.createTempFile(CodecMode.ENCODE.name().toLowerCase(), ca.name().toLowerCase());
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
			if (!outputFile.delete()) {
				outputFile.deleteOnExit();
			}
		}
		return baos.toString(CHARSET).replaceAll("[" + NewLine.CRLF.toString() + "]+", "");
	}

	private String testFileDecoder(final CodecAlgorithm ca, final File file) throws IOException {
		engine.setMode(CodecMode.DECODE);
		File outputFile = File.createTempFile(CodecMode.DECODE.name().toLowerCase(), ca.name().toLowerCase());
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
			if (!outputFile.delete()) {
				outputFile.deleteOnExit();
			}
		}
		return baos.toString(CHARSET);
	}

}
