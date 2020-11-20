package cz.gattserver.grass.control;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		// src/main/resources/favicon.png
		InputStream is = Main.class.getClassLoader().getResourceAsStream("favicon.png");
		BufferedImage image = ImageIO.read(is);
		trayIcon = new TrayIcon(image, "Grass control");
		SystemTray tray = SystemTray.getSystemTray();

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			logger.error("TrayIcon could not be added", e);
		}
	}

	public void showMessage(String message) {
		trayIcon.displayMessage("Speech recognition", message, MessageType.NONE);
	}

}
