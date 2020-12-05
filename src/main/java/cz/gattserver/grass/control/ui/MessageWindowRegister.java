package cz.gattserver.grass.control.ui;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageWindowRegister {

	private static final Logger logger = LoggerFactory.getLogger(MessageWindowRegister.class);

	// K této kolekci se musí přistupovat přes synchronized
	private static List<MessageWindow> activeWindows = new ArrayList<MessageWindow>();

	public static void registerWindow(MessageWindow caller) {
		synchronized (activeWindows) {
			logger.trace("MessageWindow '" + caller.getMessage() + "' addWindow ");
			for (MessageWindow w : activeWindows) {
				Point p = caller.getLocation();
				caller.setLocation(p.x, p.y - w.getHeight());
			}
			activeWindows.add(caller);
		}
	}

	public static void unregisterWindow(MessageWindow caller) {
		synchronized (activeWindows) {
			logger.trace("MessageWindow '" + caller.getMessage() + "' removeWindow ");
			boolean found = false;
			for (int i = 0; i < activeWindows.size(); i++) {
				MessageWindow w = activeWindows.get(i);
				if (w == caller) {
					found = true;
					continue;
				}
				if (found) {
					Point p = w.getLocation();
					w.setLocation(p.x, p.y + caller.getHeight());
				}
			}
			activeWindows.remove(caller);
		}
	}

}
