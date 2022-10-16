package cz.gattserver.grass.control.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public enum CmdControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(CmdControl.class);

	public static void openChrome(String page) {
		String command = "start chrome " + page;
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
		try {
			pb.start();
		} catch (IOException e) {
			logger.error("Command '" + command + "' failed", e);
		}
	}
	
	public static void openVLC() {
		String command = "start vlc";
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
		try {
			pb.start();
		} catch (IOException e) {
			logger.error("Command '" + command + "' failed", e);
		}
	}

	/**
	 * Vyžaduje http://www.nirsoft.net/utils/nircmd.html na PATH
	 */
	public static void callNnircmd(String cmd) {
		String command = "start nircmd " + cmd;
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
		try {
			pb.start();
		} catch (IOException e) {
			logger.error("Command '" + command + "' failed", e);
		}
	}

}
