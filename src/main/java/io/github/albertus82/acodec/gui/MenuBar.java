package io.github.albertus82.acodec.gui;

import static io.github.albertus82.acodec.gui.GuiStatus.ERROR;

import java.util.logging.Level;

import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import io.github.albertus82.acodec.common.resources.ConfigurableMessages;
import io.github.albertus82.acodec.common.resources.Language;
import io.github.albertus82.acodec.gui.listener.AboutListener;
import io.github.albertus82.acodec.gui.listener.ArmMenuListener;
import io.github.albertus82.acodec.gui.listener.ExitListener;
import io.github.albertus82.acodec.gui.listener.LanguageSelectionListener;
import io.github.albertus82.acodec.gui.listener.ProcessFileSelectionListener;
import io.github.albertus82.acodec.gui.listener.TextCopyAllSelectionListener;
import io.github.albertus82.acodec.gui.resources.GuiMessages;
import io.github.albertus82.jface.Multilanguage;
import io.github.albertus82.jface.SwtUtils;
import io.github.albertus82.jface.cocoa.CocoaEnhancerException;
import io.github.albertus82.jface.cocoa.CocoaUIEnhancer;
import io.github.albertus82.jface.i18n.LocalizedWidgets;
import io.github.albertus82.jface.sysinfo.SystemInformationDialog;
import io.github.albertus82.util.ISupplier;
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

	private final LocalizedWidgets localizedWidgets = new LocalizedWidgets();

	@NonNull
	private final MenuItem fileSaveOutputMenuItem;

	MenuBar(@NonNull final CodecGui gui) {
		final ExitListener exitListener = new ExitListener(gui);
		final AboutListener aboutListener = new AboutListener(gui);

		boolean cocoaMenuCreated = false;
		if (Util.isCocoa()) {
			try {
				new CocoaUIEnhancer(gui.getShell().getDisplay()).hookApplicationMenu(exitListener, aboutListener, null);
				cocoaMenuCreated = true;
			}
			catch (final CocoaEnhancerException e) {
				log.log(Level.WARNING, "Unable to enhance Cocoa UI:", e);
			}
		}

		final Menu bar = new Menu(gui.getShell(), SWT.BAR); // Bar

		// File
		final Menu fileMenu = new Menu(gui.getShell(), SWT.DROP_DOWN);
		final MenuItem fileMenuHeader = newLocalizedMenuItem(bar, SWT.CASCADE, "gui.label.menu.header.file");
		fileMenuHeader.setMenu(fileMenu);

		final MenuItem fileProcessMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, "gui.label.menu.item.file.process");
		fileProcessMenuItem.setEnabled(false);
		fileProcessMenuItem.addSelectionListener(new ProcessFileSelectionListener(gui));

		new MenuItem(fileMenu, SWT.SEPARATOR);

		newLocalizedMenuItem(fileMenu, SWT.PUSH, "gui.label.menu.item.file.load.input").addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.loadInput();
			}
		});

		fileSaveOutputMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, () -> messages.get("gui.label.menu.item.file.save.output") + SwtUtils.getMod1ShortcutLabel(SwtUtils.KEY_SAVE));
		fileSaveOutputMenuItem.setEnabled(false);
		fileSaveOutputMenuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				gui.saveOutput();
			}
		});
		fileSaveOutputMenuItem.setAccelerator(SWT.MOD1 | SwtUtils.KEY_SAVE);

		final ArmMenuListener fileMenuListener = e -> {
			if (gui.getAlgorithm() != null && !fileProcessMenuItem.isEnabled()) {
				fileProcessMenuItem.setEnabled(true);
			}
		};
		fileMenu.addMenuListener(fileMenuListener);
		fileMenuHeader.addArmListener(fileMenuListener);

		if (!cocoaMenuCreated) {
			new MenuItem(fileMenu, SWT.SEPARATOR);

			final MenuItem fileExitMenuItem = newLocalizedMenuItem(fileMenu, SWT.PUSH, "gui.label.menu.item.exit");
			fileExitMenuItem.addSelectionListener(exitListener);
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
			editCopyOutputTextMenuItem.setEnabled(!ERROR.equals(gui.getStatus()) && outputText != null && !outputText.isDisposed() && outputText.getCharCount() != 0);
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

		final LanguageSelectionListener languageSelectionListener = new LanguageSelectionListener(gui::setLanguage);

		for (final Language language : Language.values()) {
			final MenuItem languageMenuItem = newLocalizedMenuItem(viewLanguageSubMenu, SWT.RADIO, () -> language.getLocale().getDisplayLanguage(language.getLocale()));
			languageMenuItem.setData(language.getClass().getName(), language);
			languageMenuItem.addSelectionListener(languageSelectionListener);
			if (language.equals(messages.getLanguage())) {
				languageMenuItem.setSelection(true); // Default
			}
		}

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
		localizedWidgets.resetAllTexts();
	}

	public void notifyStatus(@NonNull final GuiStatus status) {
		fileSaveOutputMenuItem.setEnabled(GuiStatus.OK.equals(status));
	}

	private MenuItem newLocalizedMenuItem(@NonNull final Menu parent, final int style, @NonNull final String messageKey) {
		return newLocalizedMenuItem(parent, style, () -> messages.get(messageKey));
	}

	private MenuItem newLocalizedMenuItem(@NonNull final Menu parent, final int style, @NonNull final ISupplier<String> textSupplier) {
		return localizedWidgets.putAndReturn(new MenuItem(parent, style), textSupplier).getKey();
	}

}
