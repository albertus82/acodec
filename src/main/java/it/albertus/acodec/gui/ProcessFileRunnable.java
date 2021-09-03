package it.albertus.acodec.gui;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import it.albertus.acodec.common.engine.ProcessFileTask;
import it.albertus.acodec.common.resources.Messages;
import it.albertus.acodec.gui.resources.GuiMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class ProcessFileRunnable implements IRunnableWithProgress {

	private static final short TOTAL_WORK = 1000;

	private static final Messages messages = GuiMessages.INSTANCE;

	private final ProcessFileTask task;
	private String result;

	@Override
	public void run(final IProgressMonitor monitor) throws InterruptedException {
		Thread updateStatusBarThread = null;
		try {
			if (task.getInputFile() == null) {
				monitor.beginTask(messages.get("gui.message.file.process.task.name"), IProgressMonitor.UNKNOWN);
			}
			else if (task.getInputFile().length() > 0) {
				monitor.beginTask(messages.get("gui.message.file.process.task.name.file.progress", task.getInputFile().getName(), 0), TOTAL_WORK);
				updateStatusBarThread = newUpdateStatusBarThread(monitor);
				updateStatusBarThread.start();
			}
			else {
				monitor.beginTask(messages.get("gui.message.file.process.task.name.file", task.getInputFile().getName()), IProgressMonitor.UNKNOWN);
			}
			result = task.run(monitor::isCanceled).orElse(null);
		}
		catch (final CancellationException e) {
			log.log(Level.FINE, "Operation canceled:", e);
			throw new InterruptedException("Operation canceled");
		}
		catch (final Exception e) {
			throw new ProcessFileException(e);
		}
		finally {
			if (updateStatusBarThread != null) {
				updateStatusBarThread.interrupt();
			}
			monitor.done();
		}
	}

	private Thread newUpdateStatusBarThread(final IProgressMonitor monitor) {
		final Thread updateStatusBarThread = new Thread() {
			@Override
			public void run() {
				final String fileName = task.getInputFile().getName();
				final long fileLength = task.getInputFile().length();
				int done = 0;
				while (!monitor.isCanceled() && !isInterrupted()) {
					final long byteCount = task.getByteCount();
					final int partsPerThousand = (int) (byteCount / (double) fileLength * TOTAL_WORK);
					monitor.worked(partsPerThousand - done);
					done = partsPerThousand;
					monitor.setTaskName(messages.get("gui.message.file.process.task.name.file.progress", fileName, partsPerThousand / 10));
					if (byteCount >= fileLength) {
						break;
					}
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					}
					catch (final InterruptedException e) {
						interrupt();
					}
				}
			}
		};
		updateStatusBarThread.setDaemon(true); // This thread must not prevent the JVM from exiting.
		return updateStatusBarThread;
	}

	public Optional<String> getResult() {
		return Optional.ofNullable(result);
	}

}
