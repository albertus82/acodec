package it.albertus.codec.gui;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.codec.gui.listener.AboutListener;
import it.albertus.codec.gui.listener.CloseListener;
import it.albertus.codec.gui.listener.HelpMenuListener;
import it.albertus.codec.gui.listener.LanguageSelectionListener;
import it.albertus.codec.gui.listener.ProcessFileButtonSelectionListener;
import it.albertus.codec.resources.Messages;
import it.albertus.codec.resources.Messages.Language;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.jface.sysinfo.SystemInformationDialog;
import it.albertus.util.logging.LoggerFactory;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
public class MenuBar {

	private static final String LBL_MENU_HEADER_FILE = "lbl.menu.header.file";
	private static final String LBL_MENU_ITEM_PROCESS = "lbl.menu.item.process";
	private static final String LBL_MENU_ITEM_EXIT = "lbl.menu.item.exit";
	private static final String LBL_MENU_HEADER_VIEW = "lbl.menu.header.view";
	private static final String LBL_MENU_ITEM_LANGUAGE = "lbl.menu.item.language";
	private static final String LBL_MENU_HEADER_HELP = "lbl.menu.header.help";
	private static final String LBL_MENU_HEADER_HELP_WINDOWS = "lbl.menu.header.help.windows";
	private static final String LBL_MENU_ITEM_SYSTEM_INFO = "lbl.menu.item.system.info";
	private static final String LBL_MENU_ITEM_ABOUT = "lbl.menu.item.about";

	private static final Logger logger = LoggerFactory.getLogger(MenuBar.class);

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
			catch (final CocoaEnhancerException cee) {
				logger.log(Level.WARNING, Messages.get("err.cocoa.enhancer"), cee);
			}
		}

		final Menu bar = new Menu(gui.getShell(), SWT.BAR); // Bar

		// File
		final Menu fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setText(Messages.get(LBL_MENU_HEADER_FILE));
		fileMenuHeader.setMenu(fileMenu);

		fileProcessMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileProcessMenuItem.setText(Messages.get(LBL_MENU_ITEM_PROCESS));
		fileProcessMenuItem.setEnabled(false);
		fileProcessMenuItem.addSelectionListener(new ProcessFileButtonSelectionListener(gui));

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitMenuItem.setText(Messages.get(LBL_MENU_ITEM_EXIT));
			fileExitMenuItem.addSelectionListener(closeListener);
		}

		// View
		final Menu viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewMenuHeader = new MenuItem(bar, SWT.CASCADE);
		viewMenuHeader.setText(Messages.get(LBL_MENU_HEADER_VIEW));
		viewMenuHeader.setMenu(viewMenu);

		viewLanguageSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		viewLanguageSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_LANGUAGE));

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

		viewLanguageMenuItems.get(Messages.getLanguage()).setSelection(true); // Default

		// Help
		final Menu helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
		helpMenuHeader.setText(Messages.get(Util.isWindows() ? LBL_MENU_HEADER_HELP_WINDOWS : LBL_MENU_HEADER_HELP));
		helpMenuHeader.setMenu(helpMenu);

		helpSystemInfoItem = new MenuItem(helpMenu, SWT.PUSH);
		helpSystemInfoItem.setText(Messages.get(LBL_MENU_ITEM_SYSTEM_INFO));
		helpSystemInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				SystemInformationDialog.open(gui.getShell());
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
			helpAboutItem.setText(Messages.get(LBL_MENU_ITEM_ABOUT));
			helpAboutItem.addSelectionListener(new AboutListener(gui));
		}

		final HelpMenuListener helpMenuListener = new HelpMenuListener(helpSystemInfoItem);
		helpMenu.addMenuListener(helpMenuListener);
		helpMenuHeader.addArmListener(helpMenuListener);

		gui.getShell().setMenuBar(bar);
	}

	public void updateTexts() {
		fileMenuHeader.setText(Messages.get(LBL_MENU_HEADER_FILE));
		fileProcessMenuItem.setText(Messages.get(LBL_MENU_ITEM_PROCESS));
		if (fileExitMenuItem != null && !fileExitMenuItem.isDisposed()) {
			fileExitMenuItem.setText(Messages.get(LBL_MENU_ITEM_EXIT));
		}

		viewMenuHeader.setText(Messages.get(LBL_MENU_HEADER_VIEW));
		viewLanguageSubMenuItem.setText(Messages.get(LBL_MENU_ITEM_LANGUAGE));
		for (final Entry<Language, MenuItem> entry : viewLanguageMenuItems.entrySet()) {
			entry.getValue().setText(entry.getKey().getLocale().getDisplayLanguage(entry.getKey().getLocale()));
		}
		helpMenuHeader.setText(Messages.get(Util.isWindows() ? LBL_MENU_HEADER_HELP_WINDOWS : LBL_MENU_HEADER_HELP));
		helpSystemInfoItem.setText(Messages.get(LBL_MENU_ITEM_SYSTEM_INFO));
		if (helpAboutItem != null && !helpAboutItem.isDisposed()) {
			helpAboutItem.setText(Messages.get(LBL_MENU_ITEM_ABOUT));
		}
	}

	public MenuItem getFileProcessMenuItem() {
		return fileProcessMenuItem;
	}

}
