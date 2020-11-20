package cz.gattserver.grass.control.bluetooth;

import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import cz.gattserver.grass.control.vlc.VLCControl;

public enum BluetoothControl {

	INSTANCE;

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
		VLCControl vlc = null;
		while (running) {
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
					vlc = sendVLC(vlc, c);
				}
			} catch (IOException e) {
				if (isd != null)
					System.out.println("BT module disconnected");
				// nezdařilo se připojit -- počkej a zkus to znovat
				Thread.sleep(MILISEC_TO_RECONNECT);
			}
		}
		try {
			tryToCleanStreamConnection(con);
			tryToCleanDataInputStream(isd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void tryToCleanStreamConnection(StreamConnection con) throws IOException {
		if (con != null)
			con.close();
		con = null;
	}

	private void tryToCleanDataInputStream(InputStream isd) throws IOException {
		if (isd != null)
			isd.close();
		isd = null;
	}

	private void tryToCleanVLCConnection(VLCControl vlc) throws IOException {
		if (vlc != null)
			vlc.disconnect();
		vlc = null;
	}

	private VLCControl sendVLC(VLCControl vlc, int signal) throws IOException {
		if (vlc == null) {
			vlc = new VLCControl();
			vlc.connect();
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
			// nelze se poslat signál -- příkaz je zahozen
			tryToCleanVLCConnection(vlc);
		}
		return vlc;
	}

}
