package cz.gattserver.grass.control.vlc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum VLCControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(VLCControl.class);

	private VLCClient vlc;

	public void sendCommand(VLCCommand command) {
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
			}
		} catch (IOException e) {
			logger.error("SendCommand failed", e);
		}
	}

	private void tryToCleanVLCConnection() throws IOException {
		if (vlc != null)
			vlc.disconnect();
		vlc = null;
	}

}
