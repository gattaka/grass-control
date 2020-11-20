package cz.gattserver.grass.control;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CmdControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(CmdControl.class);

	public void openChrome(String page) {
		String command = "start chrome " + page;
		ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);

		// staré API... toFile :( Ale tohle se stejně nedá testovat,
		// protože OS-specific
		try {
			pb.start();
		} catch (IOException e) {
			logger.error("Command '" + command + "' failed", e);
		}
	}

}
