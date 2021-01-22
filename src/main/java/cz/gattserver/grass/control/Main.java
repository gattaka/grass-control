package cz.gattserver.grass.control;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.bluetooth.BluetoothControl;
import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.ui.MusicSearchWindow;
import cz.gattserver.grass.control.ui.TrayControl;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws IOException, InterruptedException {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		TrayControl.INSTANCE.create();
		BluetoothControl.INSTANCE.start();
		SpeechControl.INSTANCE.start();

		TrayControl.showMessage("Grass control started");
		
		MusicSearchWindow.create();

		logger.info("GrassControl initialized");
	}

}