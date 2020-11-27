package cz.gattserver.grass.control.vlc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum VLCControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(VLCControl.class);

	private static VLCClient vlc;

	public static void sendCommand(VLCCommand command) {
		try {
			try {
				if (vlc == null) {
					vlc = new VLCClient();
					vlc.connect();
				}
				vlc.sendCommand(command);
			} catch (IOException e) {
				// nelze se poslat signál -- příkaz je zahozen
				tryToCleanVLCConnection();
				logger.error("SendCommand failed", e);
			}
		} catch (IOException e) {
			logger.error("tryToCleanVLCConnection failed", e);
		}
	}

	private static void tryToCleanVLCConnection() throws IOException {
		if (vlc != null)
			vlc.disconnect();
		vlc = null;
	}

}
