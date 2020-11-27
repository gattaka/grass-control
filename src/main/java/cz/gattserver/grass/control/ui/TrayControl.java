package cz.gattserver.grass.control.ui;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.Main;
import cz.gattserver.grass.control.speech.SpeechControl;

public enum TrayControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(TrayControl.class);

	private TrayIcon trayIcon;

	public void create() throws IOException {
		// Check the SystemTray is supported
		if (!SystemTray.isSupported()) {
			logger.error("SystemTray is not supported");
			return;
		}

		PopupMenu popup = new PopupMenu();

		// src/main/resources/favicon.png
		InputStream is = Main.class.getClassLoader().getResourceAsStream("favicon.png");
		BufferedImage image = ImageIO.read(is);
		trayIcon = new TrayIcon(image, "Grass control");
		SystemTray tray = SystemTray.getSystemTray();

		CheckboxMenuItem speechRecognitionCheckbox = new CheckboxMenuItem("Speech recognition enabled", true);
		speechRecognitionCheckbox
				.addItemListener(e -> SpeechControl.INSTANCE.setEnabled(speechRecognitionCheckbox.getState()));

		MenuItem exitItem = new MenuItem("Exit");
		exitItem.addActionListener(l -> System.exit(0));

		// Add components to pop-up menu
		popup.add(speechRecognitionCheckbox);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			logger.error("TrayIcon could not be added", e);
		}
	}

	public void showMessage(String message) {
		new MessageWindow(message, MessageLevel.INFO);
	}

	public void showMessage(String message, MessageLevel level) {
		new MessageWindow(message, level);
	}

}
