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
import cz.gattserver.grass.control.ui.MessageLevel;
import cz.gattserver.grass.control.ui.TrayControl;
import cz.gattserver.grass.control.vlc.VLCCommand;
import cz.gattserver.grass.control.vlc.VLCControl;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.decoder.search.Token;

public enum SpeechControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(SpeechControl.class);

	private static final String ACOUSTIC_MODEL = "resource:/edu/cmu/sphinx/models/en-us/en-us";
	private static final String DICTIONARY_PATH = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
	private static final String GRAMMAR_PATH = "resource:/gram/";
	private static final String LANGUAGE_MODEL = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";

	private static final String VOLUME_UP = "grass control volume up";
	private static final String VOLUME_DOWN = "grass control volume down";

	private static final String GRASS_PLAYER_NEXT = "grass control player next";
	private static final String GRASS_PLAYER_PREVIOUS = "grass control player previous";
	private static final String GRASS_PLAYER_STOP = "grass control player stop";
	private static final String GRASS_PLAYER_PLAY = "grass control player play";
	private static final String GRASS_PLAYER_START_SHUFFLE = "grass control player start shuffle";
	private static final String GRASS_PLAYER_STOP_SHUFFLE = "grass control player stop shuffle";
	private static final String GRASS_PLAYER_STATUS = "grass control player status";

	private static final String OPEN_HW = "grass control open hardware";
	private static final String OPEN_GRASS = "grass control open grass";
	private static final String OPEN_NEXUS = "grass control open nexus";
	private static final String SYSTEM_MONITOR = "grass control open system monitor";
	private static final String SPEECH_HISTORY = "grass control open speech history";

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
		Configuration configuration = new Configuration();
		configuration.setAcousticModelPath(ACOUSTIC_MODEL);
		configuration.setDictionaryPath(DICTIONARY_PATH);
		configuration.setLanguageModelPath(LANGUAGE_MODEL);
		configuration.setGrammarPath(GRAMMAR_PATH);
		configuration.setUseGrammar(true);
		configuration.setGrammarName("grammar");
		// configuration.setSampleRate(48000); // přestane detekovat všechno

		LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
		SpeechResult result = null;
		recognizer.startRecognition(true);
		logger.info("Speech recognition initialized");
		while (running) {
			while ((result = recognizer.getResult()) != null) {
				String s = result.getHypothesis();
				if ("<unk>".equals(s) || "".equals(s))
					continue;
				Token bestToken = result.getResult().getBestFinalToken();
				Float score = bestToken == null ? null : bestToken.getScore();

				// Vypadá to, že čím lepší frázování (oddělení slov při
				// zadávání, tím lepší skore)
				logger.info("You said: '" + s + "' (score " + score + ")");

				switch (s) {
				case GRASS_PLAYER_NEXT:
					executeCommand(s, -1.00E8, -5.70E8, score, () -> {
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
				case GRASS_PLAYER_PREVIOUS:
					executeCommand(s, -1.00E8, -6.00E8, score, () -> {
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

				case GRASS_PLAYER_STOP:
					executeCommand(s, -1.00E8, -8.70E8, score, () -> VLCControl.sendCommand(VLCCommand.PAUSE));
					break;
				case GRASS_PLAYER_PLAY:
					executeCommand(s, -2.40E8, -5.90E8, score, () -> VLCControl.sendCommand(VLCCommand.PLAY));
					break;

				case VOLUME_UP:
					executeCommand(s, -1.88E8, -7.60E8, score, () -> CmdControl.callNnircmd("changesysvolume 2000"));
					break;
				case VOLUME_DOWN:
					executeCommand(s, -1.36E8, -7.60E8, score, () -> CmdControl.callNnircmd("changesysvolume -2000"));
					break;

				case GRASS_PLAYER_START_SHUFFLE:
					executeCommand(s, -3.10E8, -9.50E8, score, () -> VLCControl.sendCommand(VLCCommand.RANDOM_ON));
					break;
				case GRASS_PLAYER_STOP_SHUFFLE:
					executeCommand(s, -2.80E8, -7.80E8, score, () -> VLCControl.sendCommand(VLCCommand.RANDOM_OFF));
					break;

				case GRASS_PLAYER_STATUS:
					executeCommand(s, -1.00E8, -7.50E8, score, () -> runVLCStatus());
					break;

				case OPEN_HW:
					executeCommand(s, -2.40E8, -4.13E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz/hw"));
					break;
				case OPEN_GRASS:
					executeCommand(s, -2.70E8, -5.00E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz"));
					break;
				case OPEN_NEXUS:
					executeCommand(s, -2.59E8, -5.40E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz:8843"));
					break;
				case SYSTEM_MONITOR:
					executeCommand(s, -2.93E8, -5.00E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz/system-monitor"));
					break;
				case SPEECH_HISTORY:
					executeCommand(s, -2.59E8, -5.40E8, score, HistoryWindow::create);
					break;
				}
			}
		}
		recognizer.stopRecognition();
	}

	private void executeCommand(String text, double fromScore, double toScore, Float score, Command command) {
		logger.info("You said: '" + text + "' (score " + score + ")");
		if (!enabled) {
			String msg = "Speech recognition is disabled";
			TrayControl.showMessage(msg);
			logger.info(msg);
		} else if (score <= fromScore && score >= toScore) {
			history.add(new SpeechLogTO(new Date(), text, score, true));
			TrayControl.showMessage(text + " (score " + score + ")");
			logger.info("Score in range");
			try {
				command.execute();
			} catch (Exception e) {
				String msg = "Command failed";
				logger.info(msg, e);
				TrayControl.showMessage(msg, MessageLevel.ERROR);
			}
		} else {
			logger.info("Score out of range");
			history.add(new SpeechLogTO(new Date(), text, score, false));
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public static List<SpeechLogTO> getHistory() {
		return Collections.unmodifiableList(history);
	}
}
