package it.albertus.acodec.gui.listener;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.jface.sysinfo.SystemInformationDialog;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HelpMenuListener implements ArmListener, MenuListener {

	private final MenuItem item;

	@Override
	public void widgetArmed(final ArmEvent e) {
		execute();
	}

	@Override
	public void menuShown(final MenuEvent e) {
		execute();
	}

	@Override
	public void menuHidden(final MenuEvent e) {/* Ignore */}

	private void execute() {
		item.setEnabled(SystemInformationDialog.isAvailable());
	}

}