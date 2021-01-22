package cz.gattserver.grass.control.vlc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.function.Function;

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

	public static void sendCommand(VLCCommand command, String... params) {
		if (vlc == null)
			connect();
		try {
			vlc.sendCommand(command, params);
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
