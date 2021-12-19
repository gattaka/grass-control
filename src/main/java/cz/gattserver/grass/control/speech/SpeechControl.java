package cz.gattserver.grass.control.speech;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.system.CmdControl;
import cz.gattserver.grass.control.ui.HistoryWindow;
import cz.gattserver.grass.control.ui.MusicSearchWindow;
import cz.gattserver.grass.control.ui.common.MessageLevel;
import cz.gattserver.grass.control.ui.common.TrayControl;
import cz.gattserver.grass.control.vlc.VLCCommand;
import cz.gattserver.grass.control.vlc.VLCControl;

public enum SpeechControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(SpeechControl.class);

	// private static final String GRASS_CONTROL = "grass control";
	// private static final String PREFIX = GRASS_CONTROL + " ";
	private static final String PREFIX = "";

	private static final String VOLUME_UP = PREFIX + "volume up";
	private static final String VOLUME_DOWN = PREFIX + "volume down";

	private static final String PLAYER_NEXT = PREFIX + "player next";
	private static final String PLAYER_PREVIOUS = PREFIX + "player previous";
	private static final String PLAYER_STOP = PREFIX + "player stop";
	private static final String PLAYER_PLAY = PREFIX + "player play";
	private static final String PLAYER_START_SHUFFLE = PREFIX + "player start shuffle";
	private static final String PLAYER_STOP_SHUFFLE = PREFIX + "player stop shuffle";
	private static final String PLAYER_STATUS = PREFIX + "player status";

	private static final String OPEN_MUSIC = PREFIX + "open music";
	private static final String OPEN_HW = PREFIX + "open hardware";
	private static final String OPEN_GRASS = PREFIX + "open grass";
	private static final String OPEN_NEXUS = PREFIX + "open nexus";
	private static final String OPEN_SYSTEM_MONITOR = PREFIX + "open system monitor";
	private static final String OPEN_SPEECH_HISTORY = PREFIX + "open speech history";

	private volatile boolean running = false;
	private volatile boolean enabled = true;

	private static List<SpeechLogTO> history = new ArrayList<>();

	public void start() {
		if (running)
			throw new IllegalStateException(SpeechControl.class.getName() + " is already running");
		running = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					runControl();
				} catch (InterruptedException | IOException e) {
					logger.error("Thread runControl threw an exception", e);
					throw new IllegalStateException(SpeechControl.class.getName() + " was interrupted", e);
				}
			}
		}).start();
	}

	public void stop() {
		running = false;
	}

	private void runVLCStatus() {
		VLCControl.waitForResponse(str -> {
			if (str.contains("Welcome, Master"))
				return false;
			String[] chunks = str.split("\n");
			if (chunks.length != 3)
				return false;
			String[] pathChunks = chunks[0].split("/");
			String song = pathChunks[pathChunks.length - 1];
			song = song.substring(0, song.lastIndexOf(" )"));
			TrayControl.showMessage(song);
			return true;
		});
		VLCControl.sendCommand(VLCCommand.STATUS);
	}

	private void runControl() throws InterruptedException, IOException {
		VoskRecognizer recognizer = new VoskRecognizer();
		recognizer.start(this::onResult);
		logger.info("Speech recognition initialized");
	}

	private void onResult(String s) {
		switch (s) {
		case PLAYER_NEXT:
			executeCommand(s, () -> {
				VLCControl.sendCommand(VLCCommand.NEXT);
				// Musí se počkat, jinak se bude vypisovat ještě
				// aktuální položka
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				runVLCStatus();
			});
			break;
		case PLAYER_PREVIOUS:
			executeCommand(s, () -> {
				// Musí se počkat, jinak se bude vypisovat ještě
				// aktuální položka
				VLCControl.sendCommand(VLCCommand.PREV);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				runVLCStatus();
			});
			break;

		case PLAYER_STOP:
			// false -7.27
			executeCommand(s, () -> VLCControl.sendCommand(VLCCommand.PAUSE));
			break;
		case PLAYER_PLAY:
			executeCommand(s, () -> VLCControl.sendCommand(VLCCommand.PLAY));
			break;

		case VOLUME_UP:
			executeCommand(s, () -> CmdControl.callNnircmd("changesysvolume 5000"));
			break;
		case VOLUME_DOWN:
			executeCommand(s, () -> CmdControl.callNnircmd("changesysvolume -5000"));
			break;

		case PLAYER_START_SHUFFLE:
			executeCommand(s, () -> VLCControl.sendCommand(VLCCommand.RANDOM_ON));
			break;
		case PLAYER_STOP_SHUFFLE:
			executeCommand(s, () -> VLCControl.sendCommand(VLCCommand.RANDOM_OFF));
			break;

		case PLAYER_STATUS:
			executeCommand(s, () -> runVLCStatus());
			break;

		case OPEN_HW:
			executeCommand(s, () -> CmdControl.openChrome("https://www.gattserver.cz/hw"));
			break;
		case OPEN_GRASS:
			executeCommand(s, () -> CmdControl.openChrome("https://www.gattserver.cz"));
			break;
		case OPEN_NEXUS:
			executeCommand(s, () -> CmdControl.openChrome("https://www.gattserver.cz:8843"));
			break;
		case OPEN_SYSTEM_MONITOR:
			executeCommand(s, () -> CmdControl.openChrome("https://www.gattserver.cz/system-monitor"));
			break;
		case OPEN_SPEECH_HISTORY:
			executeCommand(s, HistoryWindow::create);
			break;
		case OPEN_MUSIC:
			executeCommand(s, MusicSearchWindow::showInstance);
			break;
		}
	}

	private void executeCommand(String text, Command command) {
		logger.info("You said: '" + text);
		if (!enabled) {
			String msg = "Speech recognition is disabled";
			TrayControl.showMessage(msg);
			logger.info(msg);
		} else {
			history.add(new SpeechLogTO(new Date(), text, 1f, true));
			try {
				command.execute();
			} catch (Exception e) {
				String msg = "Command failed";
				logger.info(msg, e);
				TrayControl.showMessage(msg, MessageLevel.ERROR);
			}
		}

		history.add(new SpeechLogTO(new Date(), text, 1f, false));
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public static List<SpeechLogTO> getHistory() {
		return Collections.unmodifiableList(history);
	}
}
