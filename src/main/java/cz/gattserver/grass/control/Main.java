package cz.gattserver.grass.control;

import java.io.IOException;

import cz.gattserver.grass.control.bluetooth.BluetoothControl;
import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.system.VolumeControl;
import cz.gattserver.grass.control.ui.TrayControl;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) throws IOException, InterruptedException {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		TrayControl.INSTANCE.create();
		BluetoothControl.INSTANCE.start();
		SpeechControl.INSTANCE.start();
		VolumeControl.probe();

		TrayControl.showMessage("Grass control started");
	}

}
