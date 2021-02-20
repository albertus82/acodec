package it.albertus.acodec.console;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

import it.albertus.acodec.common.engine.ProcessFileTask;
import it.albertus.acodec.console.resources.ConsoleMessages;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ProcessFileRunnable {

	@NonNull
	private final ProcessFileTask task;
	private final PrintStream out;

	public void run() throws FileNotFoundException, EncoderException, DecoderException {
		if (task.getOutputFile() == null || out == null) {
			task.run(() -> false);
		}
		else {
			Thread printProgressThread = null;
			try {
				printProgressThread = newPrintProgressThread();
				printProgressThread.start();
				task.run(() -> false);
			}
			finally {
				if (printProgressThread != null) {
					printProgressThread.interrupt();
					try {
						printProgressThread.join();
					}
					catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	private Thread newPrintProgressThread() {
		final Thread printProgressThread = new Thread() {
			@Override
			public void run() {
				final long inputFileLength = task.getInputFile().length();
				final String part1 = ConsoleMessages.get("console.message.file.process.progress") + " (";
				out.print(part1);
				int charsToDelete = 0;
				while (task.getByteCount() < inputFileLength && !isInterrupted()) {
					final StringBuilder del = new StringBuilder();
					for (short i = 0; i < charsToDelete; i++) {
						del.append('\b');
					}
					final String part2 = (int) (task.getByteCount() / (double) inputFileLength * 100) + "%)";
					out.print(del + part2);
					charsToDelete = part2.length();
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					}
					catch (final InterruptedException e) {
						interrupt();
					}
				}
				final StringBuilder del = new StringBuilder();
				for (short i = 0; i < charsToDelete + part1.length(); i++) {
					del.append("\b \b");
				}
				out.print(del);
			}
		};
		printProgressThread.setDaemon(true); // This thread must not prevent the JVM from exiting.
		return printProgressThread;
	}

}
