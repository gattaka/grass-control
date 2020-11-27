package cz.gattserver.grass.control.bluetooth;

import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.vlc.VLCCommand;
import cz.gattserver.grass.control.vlc.VLCControl;

public enum BluetoothControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(BluetoothControl.class);

	private static final String PROTOCOL_PREFIX = "btspp://";
	private static final String DEVICE_ADDRESS = "98D331B2C4A4";
	private static final String PORT_SUFFIX = ":1";
	private static final String URL = PROTOCOL_PREFIX + DEVICE_ADDRESS + PORT_SUFFIX;
	private static final int MILISEC_TO_RECONNECT = 5000;

	private volatile boolean running = false;

	public void start() {
		if (running)
			throw new IllegalStateException(BluetoothControl.class.getName() + " is already running");
		running = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					runControl();
				} catch (InterruptedException e) {
					throw new IllegalStateException(BluetoothControl.class.getName() + " was interrupted", e);
				}
			}
		}).start();
	}

	public void stop() {
		running = false;
	}

	private void runControl() throws InterruptedException {
		StreamConnection con = null;
		InputStream isd = null;
		RemoteDevice device = null;
		while (running) {
			try {
				if (con == null) {
					con = (StreamConnection) Connector.open(URL, Connector.READ, true);
					device = RemoteDevice.getRemoteDevice(con);
					isd = con.openInputStream();
					logger.info("BT module connected");
					Toolkit.getDefaultToolkit().beep();
					Thread.sleep(200);
					Toolkit.getDefaultToolkit().beep();
					Thread.sleep(200);
					Toolkit.getDefaultToolkit().beep();
				}

				// Pouze těmto voláním se dá zjistit, zda je zařízení ještě
				// stále zapnuté, nebo se vypnulo. Ostaní metody jsou
				// false-positive. Připojení se musí resetovat, protože při
				// opětovném zapnutí BT modulu se nenavazuje na otevřené
				// spojení, byť to ani samo odpojením BT modulu nespadne.
				//
				// Odpojeno -- všechno vyhoď a zkoušej se znovu připojit --
				// čekej až se znovu zapne
				if (!device.isAuthenticated())
					throw new IOException();

				while (isd.available() > 0) {
					int c = isd.read();
					sendVLCCommand(c);
				}
			} catch (IOException e) {
				if (isd != null)
					logger.info("BT module disconnected");
				// nezdařilo se připojit -- počkej a zkus to znovat
				Thread.sleep(MILISEC_TO_RECONNECT);
				try {
					con = tryToCleanStreamConnection(con);
					isd = tryToCleanDataInputStream(isd);
				} catch (IOException e2) {
					logger.info("BT StreamConnection/InputStream close failed", e2);
				}
			}
		}
	}

	private StreamConnection tryToCleanStreamConnection(StreamConnection con) throws IOException {
		if (con != null)
			con.close();
		return null;
	}

	private InputStream tryToCleanDataInputStream(InputStream isd) throws IOException {
		if (isd != null)
			isd.close();
		return null;
	}

	private void sendVLCCommand(int signal) {
		switch (signal) {
		case 0x81:
			VLCControl.sendCommand(VLCCommand.NEXT);
			break;
		case 0x82:
			VLCControl.sendCommand(VLCCommand.PREV);
			break;
		case 0x83:
			VLCControl.sendCommand(VLCCommand.VOLUP);
			break;
		case 0x84:
			VLCControl.sendCommand(VLCCommand.VOLDOWN);
			break;
		}
	}

}
