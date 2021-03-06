// Copyright 2001-2007, FreeHEP.
package org.freehep.util.io;

import static org.freehep.util.io.Ascii85.A85P1;
import static org.freehep.util.io.Ascii85.A85P2;
import static org.freehep.util.io.Ascii85.A85P3;
import static org.freehep.util.io.Ascii85.A85P4;
import static org.freehep.util.io.Ascii85.MAX_CHARS_PER_LINE;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * The ASCII85InputStream encodes binary data as ASCII base-85 encoding. The
 * exact definition of ASCII base-85 encoding can be found in the PostScript
 * Language Reference (3rd ed.) chapter 3.13.3.
 * 
 * @author Mark Donszelmann
 * @version $Id: src/main/java/org/freehep/util/io/ASCII85OutputStream.java
 *          8f5e4664c6f0 2007/01/05 22:52:09 duns $
 */
public class Ascii85OutputStream extends FilterOutputStream implements Finishable {

	private boolean end;

	private int characters;

	private final int[] b = new int[4];

	private int bIndex;

	private final int[] c = new int[5];

	/**
	 * Create an ASCII85 Output Stream from given stream
	 * 
	 * @param out output stream to use
	 */
	public Ascii85OutputStream(final OutputStream out) {
		super(out);
		characters = MAX_CHARS_PER_LINE;
		end = false;
		bIndex = 0;
	}

	@Override
	public void write(final int a) throws IOException {
		b[bIndex] = a & 0x00FF;
		bIndex++;
		if (bIndex >= b.length) {
			writeTuple();
			bIndex = 0;
		}
	}

	@Override
	public void finish() throws IOException {
		if (!end) {
			end = true;
			if (bIndex > 0) {
				writeTuple();
			}
			writeEOD();
			for (final byte bt : System.lineSeparator().getBytes(StandardCharsets.US_ASCII)) {
				super.write(bt);
			}
			flush();
			if (out instanceof Finishable) {
				((Finishable) out).finish();
			}
		}
	}

	@Override
	public void close() throws IOException {
		finish();
		super.close();
	}

	private void writeTuple() throws IOException {
		// fill the rest
		for (int i = bIndex; i < b.length; i++) {
			b[i] = 0;
		}

		// convert
		long d = ((b[0] << 24) | (b[1] << 16) | (b[2] << 8) | b[3]) & 0x00000000FFFFFFFFL;

		c[0] = (int) (d / A85P4 + '!');
		d = d % A85P4;
		c[1] = (int) (d / A85P3 + '!');
		d = d % A85P3;
		c[2] = (int) (d / A85P2 + '!');
		d = d % A85P2;
		c[3] = (int) (d / A85P1 + '!');
		c[4] = (int) (d % A85P1 + '!');

		// convert !!!!! to z
		if ((bIndex >= b.length) && (c[0] == '!') && (c[1] == '!') && (c[2] == '!') && (c[3] == '!') && (c[4] == '!')) {
			writeChar('z');
		}
		else {
			for (int i = 0; i < bIndex + 1; i++) {
				writeChar(c[i]);
			}
		}
	}

	// Fix for IO-7
	private void writeEOD() throws IOException {
		if (characters <= 1) {
			characters = MAX_CHARS_PER_LINE;
			writeNewLine();
		}
		characters -= 2;
		super.write('~');
		super.write('>');
	}

	private void writeChar(final int b) throws IOException {
		if (characters == 0) {
			characters = MAX_CHARS_PER_LINE;
			writeNewLine();
		}
		characters--;
		super.write(b);
	}

	private void writeNewLine() throws IOException {
		// write a newline
		for (int i = 0; i < System.lineSeparator().length(); i++) {
			super.write(System.lineSeparator().charAt(i));
		}
	}
}
