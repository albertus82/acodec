package it.albertus.acodec.console;

import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import it.albertus.acodec.engine.ProcessFileTask;
import it.albertus.acodec.resources.Messages;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ProcessFileRunnable implements Runnable {

	@NonNull
	private final ProcessFileTask task;
	@NonNull
	private final PrintStream out;

	@Override
	public void run() {
		if (task.getOutputFile() == null) {
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
				final String part1 = Messages.get("msg.file.process.progress") + " (";
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
