// Copyright 2001, FreeHEP.
package org.freehep.util.io;

import java.io.IOException;

/**
 * The FinishableOutputStream allows a generic way of calling finish on an
 * output stream without closing it.
 * 
 * @author Mark Donszelmann
 * @version $Id: src/main/java/org/freehep/util/io/FinishableOutputStream.java
 *          96b41b903496 2005/11/21 19:50:18 duns $
 */
@FunctionalInterface
interface Finishable {

	/**
	 * Finishes the current outputstream (compresses, flushes, caluclates CRC) and
	 * writes whatever is left in the buffers, but does not close the stream.
	 * 
	 * @throws IOException if write fails
	 */
	void finish() throws IOException;
}
