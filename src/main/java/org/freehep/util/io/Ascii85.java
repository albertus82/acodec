// Copyright 2001, FreeHEP.
package org.freehep.util.io;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for the ASCII85 encoding.
 * 
 * @author Mark Donszelmann
 * @version $Id: src/main/java/org/freehep/util/io/ASCII85.java 96b41b903496
 *          2005/11/21 19:50:18 duns $
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class Ascii85 {

	/**
	 * Maxmimum line length for ASCII85
	 */
	static final int MAX_CHARS_PER_LINE = 76;

	/**
	 * 85^1
	 */
	static final long A85P1 = 85;

	/**
	 * 85^2
	 */
	static final long A85P2 = A85P1 * A85P1;

	/**
	 * 85^3
	 */
	static final long A85P3 = A85P2 * A85P1;

	/**
	 * 85^4
	 */
	static final long A85P4 = A85P3 * A85P1;

}
