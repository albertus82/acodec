package io.github.albertus82.acodec.cli;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

import io.github.albertus82.acodec.cli.resources.ConsoleMessages;
import io.github.albertus82.acodec.common.engine.ProcessFileTask;
import io.github.albertus82.acodec.common.resources.Messages;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ProcessFileRunnable {

	private static final Messages messages = ConsoleMessages.INSTANCE;

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
				final String part1 = messages.get("console.message.file.process.progress") + " (";
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
				for (short i = 0; i < charsToDelete; i++) {
					del.append('\b');
				}
				out.println(del + "100%)");
			}
		};
		printProgressThread.setDaemon(true); // This thread must not prevent the JVM from exiting.
		return printProgressThread;
	}

}
