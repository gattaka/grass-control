package cz.gattserver.grass.control.ui.common;

public class MessageWindowRegister {

	// // K této kolekci se musí přistupovat přes synchronized
	// private static List<Popup> activeWindows = new ArrayList<Popup>();
	//
	// public static void registerWindow(Popup caller) {
	// synchronized (activeWindows) {
	// Platform.runLater(() -> {
	// for (Popup w : activeWindows)
	// caller.setY(caller.getY() - w.getHeight());
	// activeWindows.add(caller);
	// });
	// }
	// }
	//
	// public static void unregisterWindow(Popup caller) {
	// synchronized (activeWindows) {
	// Platform.runLater(() -> {
	// boolean found = false;
	// for (int i = 0; i < activeWindows.size(); i++) {
	// Popup w = activeWindows.get(i);
	// if (w == caller) {
	// found = true;
	// continue;
	// }
	// if (found)
	// w.setY(w.getY() + caller.getHeight());
	// }
	// activeWindows.remove(caller);
	// });
	// }
	// }

}
