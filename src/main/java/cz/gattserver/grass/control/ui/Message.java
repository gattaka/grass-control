package cz.gattserver.grass.control.ui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.Main;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Message {

	private static final Logger logger = LoggerFactory.getLogger(Message.class);

	private static final int DELAY = 10000;

	private Message() {
	}

	public static float toPerc(int value) {
		return value / 255f;
	}

	private static InputStream loadImageStream(String imageName) {
		return Main.class.getClassLoader().getResourceAsStream(imageName);
	}

	public static void create(String message, MessageLevel level) {
		Platform.runLater(() -> Message.createInPlatform(message, level));
	}

	private static void createInPlatform(String message, MessageLevel level) {
		logger.trace("MessageWindow '" + message + "' creation started");

		// https://stackoverflow.com/questions/24564136/javafx-can-you-create-a-stage-that-doesnt-show-on-the-task-bar-and-is-undecora
		Stage primaryStage = new Stage();
		primaryStage.initStyle(StageStyle.UTILITY);
		primaryStage.setOpacity(0);
		primaryStage.show();
		Stage stage = new Stage();
		stage.initOwner(primaryStage);
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.setAlwaysOnTop(true);

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(8));
		grid.setBackground(
				new Background(new BackgroundFill(Color.rgb(244, 241, 230, 1), new CornerRadii(3), Insets.EMPTY)));
		grid.setStyle("-fx-border-color: #aaa;");

		Scene scene = new Scene(grid);
		stage.setScene(scene);

		Image image = new Image(loadImageStream("favicon.png"));
		grid.add(new ImageView(image), 0, 0);
		grid.add(new Label("Grass control info"), 1, 0);

		String messageIconName = null;
		switch (level) {
		case ERROR:
			messageIconName = "block_16.png";
			break;
		case WARN:
			messageIconName = "warn_16.png";
			break;
		case INFO:
		default:
			messageIconName = "info_16.png";
			break;
		}

		Separator separator1 = new Separator();
		grid.add(separator1, 0, 1, 2, 1);

		Image messageImage = new Image(loadImageStream(messageIconName));
		grid.add(new ImageView(messageImage), 0, 2);
		grid.add(new Label(message), 1, 2);

		new Thread(() -> {
			MessageWindowRegister.registerWindow(stage);
		}).start();

		new Thread(() -> {
			try {
				Thread.sleep(DELAY);
				while (stage.getOpacity() > 0 && stage.isShowing()) {
					Platform.runLater(() -> stage.setOpacity(Math.max(stage.getOpacity() - 0.05f, 0)));
					Thread.sleep(50);
				}
				Platform.runLater(stage::close);
				MessageWindowRegister.unregisterWindow(stage);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				MessageWindowRegister.unregisterWindow(stage);
				stage.close();
			}
		});

		stage.show();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double w = scene.getWidth();
		double h = scene.getHeight();
		double x = screenSize.getWidth() - w - 10;
		double y = screenSize.getHeight() - h - 40;
		stage.setX(x);
		stage.setY(y);

		logger.trace("MessageWindow '" + message + "' creation done");
	}

}
