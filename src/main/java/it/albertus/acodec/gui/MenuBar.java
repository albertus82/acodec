package it.albertus.acodec.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import it.albertus.acodec.common.resources.ConfigurableMessages;
import it.albertus.acodec.common.resources.Messages.Language;
import it.albertus.acodec.gui.listener.AboutListener;
import it.albertus.acodec.gui.listener.ArmMenuListener;
import it.albertus.acodec.gui.listener.CloseListener;
import it.albertus.acodec.gui.listener.LanguageSelectionListener;
import it.albertus.acodec.gui.listener.ProcessFileButtonSelectionListener;
import it.albertus.acodec.gui.listener.TextCopyAllSelectionListener;
import it.albertus.acodec.gui.resources.GuiMessages;
import it.albertus.jface.Multilanguage;
import it.albertus.jface.cocoa.CocoaEnhancerException;
import it.albertus.jface.cocoa.CocoaUIEnhancer;
import it.albertus.jface.sysinfo.SystemInformationDialog;
import lombok.NonNull;
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

	private static final ConfigurableMessages messages = GuiMessages.INSTANCE;

	private final MenuItem fileProcessMenuItem;
	private final Map<Language, MenuItem> viewLanguageMenuItems = new EnumMap<>(Language.class);
	private final Collection<MenuItem> localizedMenuItems = new ArrayList<>();

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
		final MenuItem fileMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, "gui.label.menu.header.file");
		fileMenuHeader.setMenu(fileMenu);

		fileProcessMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, "gui.label.menu.item.file.process");
		fileProcessMenuItem.setEnabled(false);
		fileProcessMenuItem.addSelectionListener(new ProcessFileButtonSelectionListener(gui));

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			final MenuItem fileExitMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, "gui.label.menu.item.exit");
			fileExitMenuItem.addSelectionListener(closeListener);
		}

		// Edit
		final Menu editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem editMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, "gui.label.menu.header.edit");
		editMenuHeader.setMenu(editMenu);

		final MenuItem editCopyInputTextMenuItem = newLocalizedMenuItem(editMenu, SWT.PUSH, "gui.label.menu.item.edit.copy.input");
		editCopyInputTextMenuItem.addSelectionListener(new TextCopyAllSelectionListener(gui::getInputText));

		final MenuItem editCopyOutputTextMenuItem = newLocalizedMenuItem(editMenu, SWT.PUSH, "gui.label.menu.item.edit.copy.output");
		editCopyOutputTextMenuItem.addSelectionListener(new TextCopyAllSelectionListener(gui::getOutputText));

		final ArmMenuListener editMenuListener = e -> {
			final Text inputText = gui.getInputText();
			editCopyInputTextMenuItem.setEnabled(inputText != null && !inputText.isDisposed() && inputText.getCharCount() != 0);
			final Text outputText = gui.getOutputText();
			editCopyOutputTextMenuItem.setEnabled(!gui.isError() && outputText != null && !outputText.isDisposed() && outputText.getCharCount() != 0);
		};
		editMenu.addMenuListener(editMenuListener);
		editMenuHeader.addArmListener(editMenuListener);

		// View
		final Menu viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem viewMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, "gui.label.menu.header.view");
		viewMenuHeader.setMenu(viewMenu);

		final MenuItem viewLanguageSubMenuItem = newLocalizedMenuItem(viewMenu, SWT.CASCADE, "gui.label.menu.item.view.language");

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
		final MenuItem helpMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, Util.isWindows() ? "gui.label.menu.header.help.windows" : "gui.label.menu.header.help");
		helpMenuHeader.setMenu(helpMenu);

		final MenuItem helpSystemInfoItem = newLocalizedMenuItem(helpMenu, SWT.PUSH, "gui.label.menu.item.help.system.info");
		helpSystemInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				SystemInformationDialog.open(gui.getShell());
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			final MenuItem helpAboutItem = newLocalizedMenuItem(helpMenu, SWT.PUSH, "gui.label.menu.item.about");
			helpAboutItem.addSelectionListener(new AboutListener(gui));
		}

		final ArmMenuListener helpMenuListener = e -> helpSystemInfoItem.setEnabled(SystemInformationDialog.isAvailable());
		helpMenu.addMenuListener(helpMenuListener);
		helpMenuHeader.addArmListener(helpMenuListener);

		gui.getShell().setMenuBar(bar);
	}

	@Override
	public void updateLanguage() {
		for (final MenuItem menuItem : localizedMenuItems) {
			if (menuItem != null && !menuItem.isDisposed() && menuItem.getData() instanceof String) {
				menuItem.setText(messages.get(menuItem));
			}
		}
		for (final Entry<Language, MenuItem> entry : viewLanguageMenuItems.entrySet()) {
			final MenuItem menuItem = entry.getValue();
			if (menuItem != null && !menuItem.isDisposed()) {
				final Locale locale = entry.getKey().getLocale();
				menuItem.setText(locale.getDisplayLanguage(locale));
			}
		}
	}

	public void enableFileProcessMenuItem() {
		fileProcessMenuItem.setEnabled(true);
	}

	private MenuItem newLocalizedMenuItem(final Menu parent, final int style, @NonNull final String messageKey) {
		final MenuItem menuItem = new MenuItem(parent, style);
		menuItem.setData(messageKey);
		menuItem.setText(messages.get(menuItem));
		localizedMenuItems.add(menuItem);
		return menuItem;
	}

}
