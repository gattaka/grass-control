package cz.gattserver.grass.control.ui.common;

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
import cz.gattserver.grass.control.ui.HistoryWindow;
import cz.gattserver.grass.control.ui.MusicSearchWindow;

public enum TrayControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(TrayControl.class);

	private TrayIcon trayIcon;

	public static InputStream getIconStream() throws IOException {
		// src/main/resources/favicon.png
		return Main.class.getClassLoader().getResourceAsStream("favicon.png");
	}

	public static BufferedImage getIcon() throws IOException {
		BufferedImage image = ImageIO.read(getIconStream());
		return image;
	}

	public void create() throws IOException {
		// Check the SystemTray is supported
		if (!SystemTray.isSupported()) {
			logger.error("SystemTray is not supported");
			return;
		}

		PopupMenu popup = new PopupMenu();
		trayIcon = new TrayIcon(getIcon(), "Grass control");
		SystemTray tray = SystemTray.getSystemTray();

		CheckboxMenuItem speechRecognitionCheckbox = new CheckboxMenuItem("Hlasové ovládání", true);
		speechRecognitionCheckbox
				.addItemListener(e -> SpeechControl.INSTANCE.setEnabled(speechRecognitionCheckbox.getState()));
		popup.add(speechRecognitionCheckbox);

		MenuItem speechRecognitionRestartItem = new MenuItem("Restartovat hlasovou detekci");
		speechRecognitionRestartItem.addActionListener(e -> SpeechControl.INSTANCE.restart());
		popup.add(speechRecognitionRestartItem);

		MenuItem historyItem = new MenuItem("Historie příkazů");
		historyItem.addActionListener(e -> HistoryWindow.create());
		popup.add(historyItem);

		MenuItem musicItem = new MenuItem("Vyhledávání hudby");
		musicItem.addActionListener(e -> MusicSearchWindow.showInstance());
		popup.add(musicItem);

		popup.addSeparator();
		MenuItem exitItem = new MenuItem("Ukončit");
		exitItem.addActionListener(l -> System.exit(0));
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			logger.error("TrayIcon could not be added", e);
		}
	}

	public static void showMessage(String message) {
		Message.create(message, MessageLevel.INFO);
	}

	public static void showMessage(String message, MessageLevel level) {
		Message.create(message, level);
	}

}
