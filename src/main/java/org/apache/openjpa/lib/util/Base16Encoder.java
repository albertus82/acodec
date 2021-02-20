/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openjpa.lib.util;

import org.bouncycastle.util.Arrays;

import it.albertus.acodec.common.resources.Messages;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Base 16 encoder.
 *
 * @author Marc Prud'hommeaux
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base16Encoder {

	private static final char[] HEX = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * Convert bytes to a base16 string.
	 */
	public static String encode(final byte[] byteArray) {
		final StringBuilder hexBuffer = new StringBuilder(byteArray.length * 2);
		for (final byte b : byteArray) {
			for (int j = 1; j >= 0; j--) {
				hexBuffer.append(HEX[(b >> (j * 4)) & 0xF]);
			}
		}
		return hexBuffer.toString();
	}

	/**
	 * Convert a base16 string into a byte array.
	 */
	public static byte[] decode(final String s) {
		final byte[] r = new byte[s.length() / 2];
		for (int i = 0; i < r.length; i++) {
			int digit1 = s.charAt(i * 2);
			if (digit1 >= 'a') {
				digit1 -= 32; // toLowerCase
			}
			int digit2 = s.charAt(i * 2 + 1);
			if (digit2 >= 'a') {
				digit2 -= 32; // toLowerCase
			}
			if (!Arrays.contains(HEX, (char) digit1) || !Arrays.contains(HEX, (char) digit2)) {
				throw new IllegalArgumentException(Messages.get("common.err.invalid.input"));
			}

			if (digit1 >= '0' && digit1 <= '9') {
				digit1 -= '0';
			}
			else if (digit1 >= 'A' && digit1 <= 'F') {
				digit1 -= 'A' - 10;
			}

			if (digit2 >= '0' && digit2 <= '9') {
				digit2 -= '0';
			}
			else if (digit2 >= 'A' && digit2 <= 'F') {
				digit2 -= 'A' - 10;
			}

			r[i] = (byte) ((digit1 << 4) + digit2);
		}
		return r;
	}
}
