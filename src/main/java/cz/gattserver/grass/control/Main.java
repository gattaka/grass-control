package cz.gattserver.grass.control;

import java.io.IOException;

import cz.gattserver.grass.control.bluetooth.BluetoothControl;
import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.system.VolumeControl;
import cz.gattserver.grass.control.ui.TrayControl;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		TrayControl.INSTANCE.create();
		BluetoothControl.INSTANCE.start();
		SpeechControl.INSTANCE.start();
		VolumeControl.probe();

		TrayControl.showMessage("Grass control started");

	}

}
