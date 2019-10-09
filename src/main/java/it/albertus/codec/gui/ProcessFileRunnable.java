package it.albertus.codec.gui;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import it.albertus.codec.engine.CancelException;
import it.albertus.codec.engine.ProcessFileTask;
import it.albertus.codec.resources.Messages;
import it.albertus.util.ISupplier;

public class ProcessFileRunnable implements IRunnableWithProgress {

	private final ProcessFileTask task;
	private String result;

	public ProcessFileRunnable(final ProcessFileTask task) {
		this.task = task;
	}

	@Override
	public void run(final IProgressMonitor monitor) throws InterruptedException {
		monitor.beginTask(Messages.get("msg.file.process.task.name", task.getInputFile().getName(), 0), 1000);
		new Thread() {
			@Override
			public void run() {
				final long fileLength = task.getInputFile().length();
				int done = 0;
				while (!monitor.isCanceled()) {
					final long byteCount = task.getByteCount();
					final int partsPerThousand = (int) (byteCount / (double) fileLength * 1000);
					monitor.worked(partsPerThousand - done);
					done = partsPerThousand;
					monitor.setTaskName(Messages.get("msg.file.process.task.name", task.getInputFile().getName(), partsPerThousand / 10));
					if (fileLength == byteCount) {
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
		try {
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
