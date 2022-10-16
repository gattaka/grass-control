package cz.gattserver.grass.control.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class Message {

	private static final Logger logger = LoggerFactory.getLogger(Message.class);

	private static final int DELAY = 5000;

	private Message() {
	}

	public static float toPerc(int value) {
		return value / 255f;
	}

	private static InputStream loadImageStream(String imageName) {
		return Message.class.getClassLoader().getResourceAsStream(imageName);
	}

	public static void create(String message, MessageLevel level) {
//		Platform.runLater(() -> Message.createInPlatform(message, level));
	}

	private static void createInPlatform(String message, MessageLevel level) {
		logger.trace("MessageWindow '" + message + "' creation started");

//		Popup popup = new Popup();
//
//		GridPane grid = new GridPane();
//		grid.setAlignment(Pos.CENTER);
//		grid.setHgap(10);
//		grid.setVgap(10);
//		grid.setPadding(new Insets(8));
//		grid.setBackground(
//				new Background(new BackgroundFill(Color.rgb(244, 241, 230, 1), new CornerRadii(3), Insets.EMPTY)));
//		grid.setStyle("-fx-border-color: #aaa;");
//
//		popup.getContent().add(grid);
//
//		Image image = new Image(loadImageStream("favicon.png"));
//		grid.add(new ImageView(image), 0, 0);
//		grid.add(new Label("Grass control info"), 1, 0);
//
//		String messageIconName = null;
//		switch (level) {
//		case ERROR:
//			messageIconName = "block_16.png";
//			break;
//		case WARN:
//			messageIconName = "warn_16.png";
//			break;
//		case INFO:
//		default:
//			messageIconName = "info_16.png";
//			break;
//		}
//
//		Separator separator1 = new Separator();
//		grid.add(separator1, 0, 1, 2, 1);
//
//		Image messageImage = new Image(loadImageStream(messageIconName));
//		grid.add(new ImageView(messageImage), 0, 2);
//		grid.add(new Label(message), 1, 2);
//
//		new Thread(() -> {
//			MessageWindowRegister.registerWindow(popup);
//		}).start();
//
//		new Thread(() -> {
//			try {
//				Thread.sleep(DELAY);
//				while (popup.getOpacity() > 0 && popup.isShowing()) {
//					Platform.runLater(() -> popup.setOpacity(Math.max(popup.getOpacity() - 0.05f, 0)));
//					Thread.sleep(50);
//				}
//				Platform.runLater(popup::hide);
//				MessageWindowRegister.unregisterWindow(popup);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}).start();
//
//		popup.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(MouseEvent mouseEvent) {
//				MessageWindowRegister.unregisterWindow(popup);
//				popup.hide();
//			}
//		});
//
//		Screen screen = Screen.getPrimary();
//		Rectangle2D bounds = screen.getBounds();
//
//		popup.show(Main.getPrimaryStage());
//		popup.setX(bounds.getWidth() - popup.getWidth() - 10);
//		popup.setY(bounds.getHeight() - popup.getHeight() - 40);

		logger.trace("MessageWindow '" + message + "' creation done");
	}

}
