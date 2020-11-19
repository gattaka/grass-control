package cz.gattserver.vlc.remote;

import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import cz.gattserver.vlc.remote.telnet.VLCControl;

public class Main {

	private static final String PROTOCOL_PREFIX = "btspp://";
	private static final String DEVICE_ADDRESS = "98D331B2C4A4";
	private static final String PORT_SUFFIX = ":1";
	private static final String URL = PROTOCOL_PREFIX + DEVICE_ADDRESS + PORT_SUFFIX;
	private static int MILISEC_TO_RECONNECT = 5000;

	private static StreamConnection con = null;
	private static InputStream isd = null;
	private static RemoteDevice device = null;

	private static VLCControl vlc;

	private static void tryToCleanStreamConnection() {
		if (con != null) {
			try {
				con.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		con = null;
	}

	private static void tryToCleanDataInputStream() {
		if (isd != null) {
			try {
				isd.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		isd = null;
	}

	private static void tryToCleanVLCConnection() {
		if (vlc != null) {
			try {
				vlc.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		vlc = null;
	}

	private static void sendVLC(int signal) {
		try {
			if (vlc == null) {
				vlc = new VLCControl();
				vlc.connect();
			}
		} catch (IOException e) {
			// nelze se pøipojit -- pøíkaz je zahozen
			return;
		}

		try {
			switch (signal) {
			case 0x81:
				System.out.println("NEXT");
				vlc.sendCommand(VLCControl.NEXT);
				break;
			case 0x82:
				System.out.println("PREV");
				vlc.sendCommand(VLCControl.PREV);
				break;
			case 0x83:
				System.out.println("VOLUME UP");
				vlc.sendCommand(VLCControl.VOLUP);
				break;
			case 0x84:
				System.out.println("VOLUME DOWN");
				vlc.sendCommand(VLCControl.VOLDOWN);
				break;
			}
		} catch (IOException e) {
			// nelze se poslat signál-- pøíkaz je zahozen
			tryToCleanVLCConnection();
			return;
		}

	}

	public static void main(String[] args) throws InterruptedException {
		while (true) {
			try {
				if (con == null) {
					con = (StreamConnection) Connector.open(URL, Connector.READ, true);
					device = RemoteDevice.getRemoteDevice(con);
					isd = con.openInputStream();
					System.out.println("BT module connected");
					Toolkit.getDefaultToolkit().beep();
					Thread.sleep(200);
					Toolkit.getDefaultToolkit().beep();
					Thread.sleep(200);
					Toolkit.getDefaultToolkit().beep();
				}

				// Pouze tímto voláním se dá zjistit, zda je zaøízení ještì stále zapnuté, nebo se vypnulo. Ostaní
				// metody jsou false-positive. Pøipojení se musí resetovat, protože pøi opìtovném zapnutí BT modulu se
				// nenavazuje na otevøené spojení, by to ani samo odpojením BT modulu nespadne.
				if (device.isAuthenticated() == false) {
					// odpojeno -- všechno vyhoï a zkoušej se znovu pøipojit -- èekej až se znovu zapne
					throw new IOException();
				}

				while (isd.available() > 0) {
					int c = isd.read();
					sendVLC(c);
				}
			} catch (IOException e) {
				if (isd != null) {
					System.out.println("BT module disconnected");
				}
				// nezdaøilo se pøipojit/èíst -- poèkej a zkus to znovat
				try {
					Thread.sleep(MILISEC_TO_RECONNECT);
				} catch (InterruptedException e1) {
					// Chyba sleep
					System.exit(1);
				}
				tryToCleanStreamConnection();
				tryToCleanDataInputStream();
			}
		}

	}
}
