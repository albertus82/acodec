package it.albertus.codec.gui;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import it.albertus.codec.engine.CancelException;
import it.albertus.codec.engine.ProcessFileTask;
import it.albertus.codec.resources.Messages;
import it.albertus.util.ISupplier;

public class ProcessFileRunnable implements IRunnableWithProgress {

	private static final short TOTAL_WORK = 1000;

	private final ProcessFileTask task;
	private String result;

	public ProcessFileRunnable(final ProcessFileTask task) {
		this.task = task;
	}

	@Override
	public void run(final IProgressMonitor monitor) throws InterruptedException {
		final String fileName = task.getInputFile().getName();
		final long fileLength = task.getInputFile().length();
		try {
			if (fileLength <= 0) {
				monitor.beginTask(Messages.get("msg.file.process.task.name", fileName), IProgressMonitor.UNKNOWN);
			}
			else {
				monitor.beginTask(Messages.get("msg.file.process.task.name.progress", fileName, 0), TOTAL_WORK);
				new Thread() {
					@Override
					public void run() {
						int done = 0;
						while (!monitor.isCanceled()) {
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
								Thread.currentThread().interrupt();
							}
						}
					}
				}.start();
			}
			result = task.run(new ISupplier<Boolean>() {
				@Override
				public Boolean get() {
					return monitor.isCanceled();
				}
			});
		}
		catch (final CancelException e) {
			throw new InterruptedException(e.getMessage());
		}
		finally {
			monitor.done();
		}
	}

	public String getResult() {
		return result;
	}

}
