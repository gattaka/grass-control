package cz.gattserver.grass.control;

import java.io.IOException;

import cz.gattserver.grass.control.bluetooth.BluetoothControl;
import cz.gattserver.grass.control.speech.SpeechControl;

public class Main {

	public static void main(String[] args) throws IOException {
		TrayControl.INSTANCE.create();
		BluetoothControl.INSTANCE.start();
		SpeechControl.INSTANCE.start();
	}

}
