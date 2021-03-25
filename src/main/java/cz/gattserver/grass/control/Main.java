package cz.gattserver.grass.control;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.bluetooth.BluetoothControl;
import cz.gattserver.grass.control.speech.SpeechControl;
import cz.gattserver.grass.control.ui.common.TrayControl;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static Stage primaryStage;

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Main.primaryStage = stage;
		stage.setX(-1);
		stage.setY(-1);
		stage.setWidth(1);
		stage.setHeight(1);
		stage.initStyle(StageStyle.UTILITY);
		stage.show();

		TrayControl.INSTANCE.create();
		BluetoothControl.INSTANCE.start();
		SpeechControl.INSTANCE.start();

		TrayControl.showMessage("Grass control started");

		logger.info("GrassControl initialized");
	}

}