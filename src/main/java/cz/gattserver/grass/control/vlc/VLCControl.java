package cz.gattserver.grass.control.vlc;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum VLCControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(VLCControl.class);

	private static VLCClient vlc;

	private static void connect() {
		try {
			vlc = new VLCClient();
			vlc.connect();
		} catch (IOException e) {
			throw new IllegalStateException("VLC connect failed", e);
		}
	}

	public static void sendCommand(VLCCommand command) {
		if (vlc == null)
			connect();
		try {
			vlc.sendCommand(command);
		} catch (IOException e1) {
			logger.error("SendCommand failed -- trying reconnect");
			try {
				vlc.disconnect();
			} catch (IOException ee) {
				// nevadí, disconnect nemusí projít, pokud je spojení úplně
				// zrušené, je dobré to ale zkusit, aby nezůstalo viset
			}
			// zkus vytvořit nové spojení
			connect();
			try {
				vlc.sendCommand(command);
			} catch (IOException ee) {
				// pokud ani s novým spojením příkaz opět nejde, vzdej to
				throw new IllegalStateException("SendCommand failed (after reconnect)", ee);
			}
		}
	}

}
