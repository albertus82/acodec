/*
 * basE91 command line front-end
 *
 * Copyright (c) 2000-2006 Joachim Henke
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of Joachim Henke nor the names of his contributors may
 *    be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sourceforce.base91;

import java.io.*;

public class b91cli
{
	public static void encode(InputStream is, OutputStream os)
	{
		int s;
		byte[] ibuf = new byte[53248];
		byte[] obuf = new byte[65536];
		basE91 b91 = new basE91();

		try {
			while ((s = is.read(ibuf)) > 0) {
				s = b91.encode(ibuf, s, obuf);
				os.write(obuf, 0, s);
			}
			s = b91.encEnd(obuf);
			os.write(obuf, 0, s);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public static void encodeWrap(InputStream is, OutputStream os)
	{
		int i, s;
		int n = 0;
		byte[] ibuf = new byte[53248];
		byte[] obuf = new byte[65536];
		char[] line = new char[76];
		basE91 b91 = new basE91();

		try {
			PrintStream ps = new PrintStream(os, false, "US-ASCII");

			while ((s = is.read(ibuf)) > 0) {
				s = b91.encode(ibuf, s, obuf);
				for (i = 0; i < s; ++i) {
					line[n++] = (char) obuf[i];
					if (n == 76) {
						ps.println(line);
						n = 0;
					}
				}
			}
			s = b91.encEnd(obuf);
			for (i = 0; i < s; ++i) {
				line[n++] = (char) obuf[i];
				if (n == 76) {
					ps.println(line);
					n = 0;
				}
			}
			if (n > 0)
				ps.println(new String(line, 0, n));
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public static void decode(InputStream is, OutputStream os)
	{
		int s;
		byte[] ibuf = new byte[65536];
		byte[] obuf = new byte[57344];
		basE91 b91 = new basE91();

		try {
			while ((s = is.read(ibuf)) > 0) {
				s = b91.decode(ibuf, s, obuf);
				os.write(obuf, 0, s);
			}
			s = b91.decEnd(obuf);
			os.write(obuf, 0, s);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private static void errExit(String msg)
	{
		System.err.println("syntax error - " + msg + "\nTry `-h' option for more information.");
		System.exit(3);
	}

	public static void main(String[] args)
	{
		int i;
		boolean enc = true;
		boolean lbr = true;
		String ifn = null;
		String ofn = null;

		for (i = 0; i < args.length; ++i)
			if (args[i].length() == 2 && args[i].charAt(0) == '-')
				switch (args[i].charAt(1)) {
				case 'd':
					enc = false;
					break;
				case 'u':
					lbr = false;
					break;
				case 'h':
					System.out.println("Usage: base91 [OPTION] infile [outfile]\n\n  -d\tdecode a basE91 encoded file\n  -u\tleave encoder output unformatted (disable line wrapping)\n  -h\tdisplay this help and exit\n  -V\toutput version information and exit");
					return;
				case 'V':
					System.out.println("base91 0.6.0\nCopyright (c) 2000-2006 Joachim Henke");
					return;
				default:
					errExit("invalid option: " + args[i]);
				}
			else if (ifn == null)
				ifn = args[i];
			else if (ofn == null)
				ofn = args[i];
			else
				errExit("too many arguments: " + args[i]);
		if (ifn == null)
			errExit("file name missing");
		if (ofn == null)
			if (enc)
				ofn = ifn + (lbr ? "_b91.txt" : ".b91");
			else {
				String lifn = ifn.toLowerCase();
				if (ifn.length() > 4 && lifn.endsWith(".b91"))
					ofn = ifn.substring(0, ifn.length() - 4);
				else if (ifn.length() > 8 && lifn.endsWith("_b91.txt"))
					ofn = ifn.substring(0, ifn.length() - 8);
				else
					ofn = ifn + ".bin";
			}

		try {
			FileInputStream ifs = new FileInputStream(ifn);
			FileOutputStream ofs = new FileOutputStream(ofn);

			if (enc)
				if (lbr)
					encodeWrap(ifs, ofs);
				else
					encode(ifs, ofs);
			else
				decode(ifs, ofs);
			ifs.close();
			ofs.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
