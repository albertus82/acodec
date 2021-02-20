package it.albertus.acodec.gui;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.acodec.common.resources.Messages.Language;
import it.albertus.acodec.gui.listener.AboutListener;
import it.albertus.acodec.gui.listener.CloseListener;
import it.albertus.acodec.gui.listener.HelpMenuListener;
import it.albertus.acodec.gui.listener.LanguageSelectionListener;
import it.albertus.acodec.gui.listener.ProcessFileButtonSelectionListener;
import it.albertus.acodec.gui.resources.GuiMessages;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.jface.sysinfo.SystemInformationDialog;
import lombok.extern.java.Log;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
@Log
public class MenuBar implements Multilanguage {

	private static final GuiMessages messages = GuiMessages.INSTANCE;

	private final MenuItem fileMenuHeader;
	private final MenuItem fileProcessMenuItem;
	private MenuItem fileExitMenuItem;

	private final MenuItem viewMenuHeader;
	private final MenuItem viewLanguageSubMenuItem;
	private final Map<Language, MenuItem> viewLanguageMenuItems = new EnumMap<>(Language.class);

	private final MenuItem helpMenuHeader;
	private final MenuItem helpSystemInfoItem;
	private MenuItem helpAboutItem;

	MenuBar(final CodecGui gui) {
		final CloseListener closeListener = new CloseListener(gui);
		final AboutListener aboutListener = new AboutListener(gui);

		boolean cocoaMenuCreated = false;
		if (Util.isCocoa()) {
			try {
				new CocoaUIEnhancer(gui.getShell().getDisplay()).hookApplicationMenu(closeListener, aboutListener, null);
				cocoaMenuCreated = true;
			}
			catch (final CocoaEnhancerException e) {
				log.log(Level.WARNING, messages.get("gui.error.cocoa.enhancer"), e);
			}
		}

		final Menu bar = new Menu(gui.getShell(), SWT.BAR); // Bar

		// File
		final Menu fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setData("gui.label.menu.header.file");
		fileMenuHeader.setText(messages.get(fileMenuHeader));
		fileMenuHeader.setMenu(fileMenu);

		fileProcessMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileProcessMenuItem.setData("gui.label.menu.item.process");
		fileProcessMenuItem.setText(messages.get(fileProcessMenuItem));
		fileProcessMenuItem.setEnabled(false);
		fileProcessMenuItem.addSelectionListener(new ProcessFileButtonSelectionListener(gui));

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitMenuItem.setData("gui.label.menu.item.exit");
			fileExitMenuItem.setText(messages.get(fileExitMenuItem));
			fileExitMenuItem.addSelectionListener(closeListener);
		}

		// View
		final Menu viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewMenuHeader = new MenuItem(bar, SWT.CASCADE);
		viewMenuHeader.setData("gui.label.menu.header.view");
		viewMenuHeader.setText(messages.get(viewMenuHeader));
		viewMenuHeader.setMenu(viewMenu);

		viewLanguageSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		viewLanguageSubMenuItem.setData("gui.label.menu.item.language");
		viewLanguageSubMenuItem.setText(messages.get(viewLanguageSubMenuItem));

		final Menu viewLanguageSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewLanguageSubMenuItem.setMenu(viewLanguageSubMenu);

		final LanguageSelectionListener languageSelectionListener = new LanguageSelectionListener(gui);

		for (final Language language : Language.values()) {
			final MenuItem languageMenuItem = new MenuItem(viewLanguageSubMenu, SWT.RADIO);
			languageMenuItem.setText(language.getLocale().getDisplayLanguage(language.getLocale()));
			languageMenuItem.setData(language);
			languageMenuItem.addSelectionListener(languageSelectionListener);
			viewLanguageMenuItems.put(language, languageMenuItem);
		}

		viewLanguageMenuItems.get(messages.getLanguage()).setSelection(true); // Default

		// Help
		final Menu helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
		helpMenuHeader.setData(Util.isWindows() ? "gui.label.menu.header.help.windows" : "gui.label.menu.header.help");
		helpMenuHeader.setText(messages.get(helpMenuHeader));
		helpMenuHeader.setMenu(helpMenu);

		helpSystemInfoItem = new MenuItem(helpMenu, SWT.PUSH);
		helpSystemInfoItem.setData("gui.label.menu.item.system.info");
		helpSystemInfoItem.setText(messages.get(helpSystemInfoItem));
		helpSystemInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				SystemInformationDialog.open(gui.getShell());
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
			helpAboutItem.setData("gui.label.menu.item.about");
			helpAboutItem.setText(messages.get(helpAboutItem));
			helpAboutItem.addSelectionListener(new AboutListener(gui));
		}

		final HelpMenuListener helpMenuListener = new HelpMenuListener(helpSystemInfoItem);
		helpMenu.addMenuListener(helpMenuListener);
		helpMenuHeader.addArmListener(helpMenuListener);

		gui.getShell().setMenuBar(bar);
	}

	@Override
	public void updateLanguage() {
		fileMenuHeader.setText(messages.get(fileMenuHeader));
		fileProcessMenuItem.setText(messages.get(fileProcessMenuItem));
		if (fileExitMenuItem != null && !fileExitMenuItem.isDisposed()) {
			fileExitMenuItem.setText(messages.get(fileExitMenuItem));
		}

		viewMenuHeader.setText(messages.get(viewMenuHeader));
		viewLanguageSubMenuItem.setText(messages.get(viewLanguageSubMenuItem));
		for (final Entry<Language, MenuItem> entry : viewLanguageMenuItems.entrySet()) {
			entry.getValue().setText(entry.getKey().getLocale().getDisplayLanguage(entry.getKey().getLocale()));
		}
		helpMenuHeader.setText(messages.get(helpMenuHeader));
		helpSystemInfoItem.setText(messages.get(helpSystemInfoItem));
		if (helpAboutItem != null && !helpAboutItem.isDisposed()) {
			helpAboutItem.setText(messages.get(helpAboutItem));
		}
	}

	public void enableFileProcessMenuItem() {
		fileProcessMenuItem.setEnabled(true);
	}

}
