package it.albertus.acodec.gui;

import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import it.albertus.acodec.engine.ProcessFileTask;
import it.albertus.acodec.resources.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessFileRunnable implements IRunnableWithProgress {

	private static final short TOTAL_WORK = 1000;

	private final ProcessFileTask task;
	@Getter
	private String result;

	@Override
	public void run(final IProgressMonitor monitor) throws InterruptedException {
		Thread updateStatusBarThread = null;
		try {
			if (task.getInputFile().length() > 0) {
				monitor.beginTask(Messages.get("msg.file.process.task.name.progress", task.getInputFile().getName(), 0), TOTAL_WORK);
				updateStatusBarThread = newUpdateStatusBarThread(monitor);
				updateStatusBarThread.start();
			}
			else {
				monitor.beginTask(Messages.get("msg.file.process.task.name", task.getInputFile().getName()), IProgressMonitor.UNKNOWN);
			}
			result = task.run(monitor::isCanceled);
		}
		catch (final CancellationException e) {
			throw new InterruptedException(e.getMessage());
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
					monitor.setTaskName(Messages.get("msg.file.process.task.name.progress", fileName, partsPerThousand / 10));
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

}
