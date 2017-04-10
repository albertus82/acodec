package it.albertus.codec.gui;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import it.albertus.codec.gui.listener.AboutListener;
import it.albertus.codec.gui.listener.CloseListener;
import it.albertus.codec.gui.listener.LanguageSelectionListener;
import it.albertus.codec.gui.listener.ProcessFileButtonSelectionListener;
import it.albertus.codec.resources.Messages;
import it.albertus.codec.resources.Messages.Language;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.util.logging.LoggerFactory;

/**
 * Solo i <tt>MenuItem</tt> che fanno parte di una barra dei men&ugrave; con
 * stile <tt>SWT.BAR</tt> hanno gli acceleratori funzionanti; negli altri casi
 * (ad es. <tt>SWT.POP_UP</tt>), bench&eacute; vengano visualizzate le
 * combinazioni di tasti, gli acceleratori non funzioneranno e le relative
 * combinazioni di tasti saranno ignorate.
 */
public class MenuBar {

	private static final Logger logger = LoggerFactory.getLogger(MenuBar.class);

	private final Menu bar;

	private final Menu fileMenu;
	private final MenuItem fileMenuHeader;
	private final MenuItem fileProcessMenuItem;
	private MenuItem fileExitMenuItem;

	private final Menu viewMenu;
	private final MenuItem viewMenuHeader;
	private final Menu viewLanguageSubMenu;
	private final MenuItem viewLanguageSubMenuItem;
	private final Map<Language, MenuItem> viewLanguageMenuItems = new EnumMap<>(Language.class);

	private Menu helpMenu;
	private MenuItem helpMenuHeader;
	private MenuItem helpAboutMenuItem;

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

		bar = new Menu(gui.getShell(), SWT.BAR); // Barra

		// File
		fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		fileMenuHeader.setText(Messages.get("lbl.menu.header.file"));
		fileMenuHeader.setMenu(fileMenu);

		fileProcessMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		fileProcessMenuItem.setText(Messages.get("lbl.menu.item.process"));
		fileProcessMenuItem.addSelectionListener(new ProcessFileButtonSelectionListener(gui));

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
			fileExitMenuItem.setText(Messages.get("lbl.menu.item.exit"));
			fileExitMenuItem.addSelectionListener(closeListener);
		}

		// View
		viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		viewMenuHeader = new MenuItem(bar, SWT.CASCADE);
		viewMenuHeader.setText(Messages.get("lbl.menu.header.view"));
		viewMenuHeader.setMenu(viewMenu);

		viewLanguageSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		viewLanguageSubMenuItem.setText(Messages.get("lbl.menu.item.language"));

		viewLanguageSubMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
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
		if (!cocoaMenuCreated) {
			helpMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
			helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
			helpMenuHeader.setText(Messages.get("lbl.menu.header.help"));
			helpMenuHeader.setMenu(helpMenu);

			helpAboutMenuItem = new MenuItem(helpMenu, SWT.PUSH);
			helpAboutMenuItem.setText(Messages.get("lbl.menu.item.about"));
			helpAboutMenuItem.addSelectionListener(aboutListener);
		}

		gui.getShell().setMenuBar(bar);
	}

	public void updateTexts() {
		fileMenuHeader.setText(Messages.get("lbl.menu.header.file"));
		fileProcessMenuItem.setText(Messages.get("lbl.menu.item.process"));
		if (fileExitMenuItem != null && !fileExitMenuItem.isDisposed()) {
			fileExitMenuItem.setText(Messages.get("lbl.menu.item.exit"));
		}

		viewMenuHeader.setText(Messages.get("lbl.menu.header.view"));
		viewLanguageSubMenuItem.setText(Messages.get("lbl.menu.item.language"));
		for (final Entry<Language, MenuItem> entry : viewLanguageMenuItems.entrySet()) {
			entry.getValue().setText(entry.getKey().getLocale().getDisplayLanguage(entry.getKey().getLocale()));
		}
		if (helpMenuHeader != null && !helpMenuHeader.isDisposed()) {
			helpMenuHeader.setText(Messages.get("lbl.menu.header.help"));
			helpAboutMenuItem.setText(Messages.get("lbl.menu.item.about"));
		}
	}

	public Map<Language, MenuItem> getViewLanguageMenuItems() {
		return Collections.unmodifiableMap(viewLanguageMenuItems);
	}

	public Menu getBar() {
		return bar;
	}

	public Menu getFileMenu() {
		return fileMenu;
	}

	public MenuItem getFileMenuHeader() {
		return fileMenuHeader;
	}

	public MenuItem getFileProcessMenuItem() {
		return fileProcessMenuItem;
	}

	public MenuItem getFileExitMenuItem() {
		return fileExitMenuItem;
	}

	public Menu getViewMenu() {
		return viewMenu;
	}

	public MenuItem getViewMenuHeader() {
		return viewMenuHeader;
	}

	public Menu getViewLanguageSubMenu() {
		return viewLanguageSubMenu;
	}

	public MenuItem getViewLanguageSubMenuItem() {
		return viewLanguageSubMenuItem;
	}

	public Menu getHelpMenu() {
		return helpMenu;
	}

	public MenuItem getHelpMenuHeader() {
		return helpMenuHeader;
	}

	public MenuItem getHelpAboutMenuItem() {
		return helpAboutMenuItem;
	}

}
