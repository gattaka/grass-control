package cz.gattserver.grass.control.vlc;

import cz.gattserver.grass.control.system.CmdControl;
import cz.gattserver.grass.control.ui.common.MessageLevel;
import cz.gattserver.grass.control.ui.common.TrayControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.function.Function;

public enum VLCControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(VLCControl.class);

	private static VLCClient vlc;

	private static void connect() {
		try {
			if (vlc != null)
				vlc.disconnect();
			vlc = new VLCClient();
			vlc.connect();
		} catch (IOException e) {
			CmdControl.openVLC();
			vlc = new VLCClient();
			try {
				vlc.connect();
			} catch (IOException e1) {
				String msg = "Couldn't connect to VLC";
				logger.warn(msg, e);
				TrayControl.showMessage(msg + " " + e.getMessage(), MessageLevel.ERROR);
			}
		}
	}

	public static void sendCommand(VLCCommand command, String... params) {
		boolean needsConnect = vlc == null || !vlc.isConnected() || !vlc.isAvailable();
		if (needsConnect)
			connect();
		try {
			// DUMMY command pro oveření, že je spojení aktivní (nic jiného
			// nefunguje tak rychle a spolehlivě)
			vlc.sendCommand(VLCCommand.RATE, "1");
			vlc.sendCommand(command, params);
		} catch (IOException e1) {
			logger.error("SendCommand failed -- trying reconnect");
			// zkus vytvořit nové spojení
			connect();
			try {
				vlc.sendCommand(command, params);
			} catch (IOException ee) {
				// pokud ani s novým spojením příkaz opět nejde, vzdej to
				throw new IllegalStateException("SendCommand failed (after reconnect)", ee);
			}
		}
	}

	public static void waitForResponse(Function<String, Boolean> callback) {
		if (vlc == null)
			connect();
		vlc.getSupport().addPropertyChangeListener(VLCClient.CLIENT_MESSAGE, new PropertyChangeListener() {
			private StringBuilder sb = new StringBuilder();

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String s = (String) evt.getNewValue();
				sb.append(s);
				String delimiter = "> ";
				if (s.contains(delimiter)) {
					String val = sb.toString();
					if (callback.apply(val.substring(0, val.indexOf(delimiter)))) {
						vlc.getSupport().removePropertyChangeListener(VLCClient.CLIENT_MESSAGE, this);
					} else {
						sb = new StringBuilder();
					}
				}

			}
		});
	}

}
