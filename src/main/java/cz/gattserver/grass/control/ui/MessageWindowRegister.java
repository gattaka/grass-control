package cz.gattserver.grass.control.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.stage.Stage;

public class MessageWindowRegister {

	// K této kolekci se musí přistupovat přes synchronized
	private static List<Stage> activeWindows = new ArrayList<Stage>();

	public static void registerWindow(Stage caller) {
		synchronized (activeWindows) {
			Platform.runLater(() -> {
				for (Stage w : activeWindows)
					caller.setY(caller.getY() - w.getHeight());
				activeWindows.add(caller);
			});
		}
	}

	public static void unregisterWindow(Stage caller) {
		synchronized (activeWindows) {
			Platform.runLater(() -> {
				boolean found = false;
				for (int i = 0; i < activeWindows.size(); i++) {
					Stage w = activeWindows.get(i);
					if (w == caller) {
						found = true;
						continue;
					}
					if (found)
						w.setY(w.getY() + caller.getHeight());
				}
				activeWindows.remove(caller);
			});
		}
	}

}
