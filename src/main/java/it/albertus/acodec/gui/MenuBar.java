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
		final MenuItem fileMenuHeader = new MenuItem(bar, SWT.CASCADE);
		localizedMenuItems.add(fileMenuHeader);
		fileMenuHeader.setData("gui.label.menu.header.file");
		fileMenuHeader.setText(messages.get(fileMenuHeader));
		fileMenuHeader.setMenu(fileMenu);

		fileProcessMenuItem = new MenuItem(fileMenu, SWT.PUSH);
		localizedMenuItems.add(fileProcessMenuItem);
		fileProcessMenuItem.setData("gui.label.menu.item.file.process");
		fileProcessMenuItem.setText(messages.get(fileProcessMenuItem));
		fileProcessMenuItem.setEnabled(false);
		fileProcessMenuItem.addSelectionListener(new ProcessFileButtonSelectionListener(gui));

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			final MenuItem fileExitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
			localizedMenuItems.add(fileExitMenuItem);
			fileExitMenuItem.setData("gui.label.menu.item.exit");
			fileExitMenuItem.setText(messages.get(fileExitMenuItem));
			fileExitMenuItem.addSelectionListener(closeListener);
		}

		// Edit
		final Menu editMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem editMenuHeader = new MenuItem(bar, SWT.CASCADE);
		localizedMenuItems.add(editMenuHeader);
		editMenuHeader.setData("gui.label.menu.header.edit");
		editMenuHeader.setText(messages.get(editMenuHeader));
		editMenuHeader.setMenu(editMenu);

		final MenuItem editCopyInputTextMenuItem = new MenuItem(editMenu, SWT.PUSH);
		localizedMenuItems.add(editCopyInputTextMenuItem);
		editCopyInputTextMenuItem.setData("gui.label.menu.item.edit.copy.input");
		editCopyInputTextMenuItem.setText(messages.get(editCopyInputTextMenuItem));
		editCopyInputTextMenuItem.addSelectionListener(new TextCopyAllSelectionListener(gui::getInputText));

		final MenuItem editCopyOutputTextMenuItem = new MenuItem(editMenu, SWT.PUSH);
		localizedMenuItems.add(editCopyOutputTextMenuItem);
		editCopyOutputTextMenuItem.setData("gui.label.menu.item.edit.copy.output");
		editCopyOutputTextMenuItem.setText(messages.get(editCopyOutputTextMenuItem));
		editCopyOutputTextMenuItem.addSelectionListener(new TextCopyAllSelectionListener(gui::getOutputText));

		final ArmMenuListener editMenuListener = e -> {
			final Text inputText = gui.getInputText();
			editCopyInputTextMenuItem.setEnabled(inputText != null && !inputText.isDisposed() && !inputText.getText().isEmpty());
			final Text outputText = gui.getOutputText();
			editCopyOutputTextMenuItem.setEnabled(!gui.isError() && outputText != null && !outputText.isDisposed() && !outputText.getText().isEmpty());
		};
		editMenu.addMenuListener(editMenuListener);
		editMenuHeader.addArmListener(editMenuListener);

		// View
		final Menu viewMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem viewMenuHeader = new MenuItem(bar, SWT.CASCADE);
		localizedMenuItems.add(viewMenuHeader);
		viewMenuHeader.setData("gui.label.menu.header.view");
		viewMenuHeader.setText(messages.get(viewMenuHeader));
		viewMenuHeader.setMenu(viewMenu);

		final MenuItem viewLanguageSubMenuItem = new MenuItem(viewMenu, SWT.CASCADE);
		localizedMenuItems.add(viewLanguageSubMenuItem);
		viewLanguageSubMenuItem.setData("gui.label.menu.item.view.language");
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
		final MenuItem helpMenuHeader = new MenuItem(bar, SWT.CASCADE);
		localizedMenuItems.add(helpMenuHeader);
		helpMenuHeader.setData(Util.isWindows() ? "gui.label.menu.header.help.windows" : "gui.label.menu.header.help");
		helpMenuHeader.setText(messages.get(helpMenuHeader));
		helpMenuHeader.setMenu(helpMenu);

		final MenuItem helpSystemInfoItem = new MenuItem(helpMenu, SWT.PUSH);
		localizedMenuItems.add(helpSystemInfoItem);
		helpSystemInfoItem.setData("gui.label.menu.item.help.system.info");
		helpSystemInfoItem.setText(messages.get(helpSystemInfoItem));
		helpSystemInfoItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				SystemInformationDialog.open(gui.getShell());
			}
		});

		if (!cocoaMenuCreated) {
			new MenuItem(helpMenu, SWT.SEPARATOR);

			final MenuItem helpAboutItem = new MenuItem(helpMenu, SWT.PUSH);
			localizedMenuItems.add(helpAboutItem);
			helpAboutItem.setData("gui.label.menu.item.about");
			helpAboutItem.setText(messages.get(helpAboutItem));
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

}
