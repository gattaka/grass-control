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
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.decoder.search.Token;

public enum SpeechControl {

	INSTANCE;

	private static final Logger logger = LoggerFactory.getLogger(SpeechControl.class);

	private static final String ACOUSTIC_MODEL = "resource:/edu/cmu/sphinx/models/en-us/en-us";
	private static final String DICTIONARY_PATH = "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
	private static final String GRAMMAR_PATH = "resource:/gram/";
	private static final String LANGUAGE_MODEL = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";

	private static final String GRASS_CONTROL = "grass control";

	private static final String PREFIX = GRASS_CONTROL + " ";

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
	private volatile boolean restart = false;
	private volatile boolean enabled = true;

	// private volatile boolean ready = false;
	// private volatile long readyTime = 0;

	private static List<SpeechLogTO> history = new ArrayList<>();

	public void restart() {
		restart = true;
	}

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
		while (true) {
			Configuration configuration = new Configuration();
			configuration.setAcousticModelPath(ACOUSTIC_MODEL);
			configuration.setDictionaryPath(DICTIONARY_PATH);
			configuration.setLanguageModelPath(LANGUAGE_MODEL);
			configuration.setGrammarPath(GRAMMAR_PATH);
			configuration.setUseGrammar(true);
			configuration.setGrammarName("grammar");
			// configuration.setSampleRate(48000); // přestane detekovat všechno

			CustomLiveSpeechRecognizer recognizer = new CustomLiveSpeechRecognizer(configuration);
			recognizer.startRecognition(true);
			logger.info("Speech recognition initialized");

			while (true) {
				if (restart) {
					restart = false;
					recognizer.stopRecognition();
					break;
				}
				SpeechResult result = recognizer.getResult();
				if (result == null)
					continue;
				Token bestToken = result.getResult().getBestFinalToken();
				if (bestToken == null)
					continue;

				String s = bestToken.getWordPathNoFiller();
				ScoreTO score = new ScoreTO(bestToken.getScore(), bestToken.getAcousticScore(),
						bestToken.getLanguageScore());

				if ("<unk>".equals(s) || "".equals(s))
					continue;

				// Vypadá to, že čím lepší frázování (oddělení slov při
				// zadávání, tím lepší skore)
				logger.info("You said: '" + s + "' (score " + score + ")");

				switch (s) {
				case PLAYER_NEXT:
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
				case PLAYER_PREVIOUS:
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

				case PLAYER_STOP:
					// false -7.27
					executeCommand(s, -1.00E8, -7.00E8, score, () -> VLCControl.sendCommand(VLCCommand.PAUSE));
					break;
				case PLAYER_PLAY:
					executeCommand(s, -1.20E8, -5.90E8, score, () -> VLCControl.sendCommand(VLCCommand.PLAY));
					break;

				case VOLUME_UP:
					executeCommand(s, -1.40E8, -7.60E8, score, () -> CmdControl.callNnircmd("changesysvolume 5000"));
					break;
				case VOLUME_DOWN:
					executeCommand(s, -1.36E8, -7.60E8, score, () -> CmdControl.callNnircmd("changesysvolume -5000"));
					break;

				case PLAYER_START_SHUFFLE:
					executeCommand(s, -2.30E8, -9.50E8, score, () -> VLCControl.sendCommand(VLCCommand.RANDOM_ON));
					break;
				case PLAYER_STOP_SHUFFLE:
					executeCommand(s, -2.30E8, -7.80E8, score, () -> VLCControl.sendCommand(VLCCommand.RANDOM_OFF));
					break;

				case PLAYER_STATUS:
					executeCommand(s, -1.00E8, -7.50E8, score, () -> runVLCStatus());
					break;

				case OPEN_HW:
					executeCommand(s, -1.60E8, -4.13E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz/hw"));
					break;
				case OPEN_GRASS:
					executeCommand(s, -1.60E8, -5.00E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz"));
					break;
				case OPEN_NEXUS:
					executeCommand(s, -1.60E8, -5.40E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz:8843"));
					break;
				case OPEN_SYSTEM_MONITOR:
					executeCommand(s, -2.10E8, -5.00E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz/system-monitor"));
					break;
				case OPEN_SPEECH_HISTORY:
					executeCommand(s, -2.10E8, -5.40E8, score, HistoryWindow::create);
					break;
				case OPEN_MUSIC:
					executeCommand(s, -2.10E8, -5.40E8, score, MusicSearchWindow::showInstance);
					break;
				}
			}
		}
	}

	private void executeCommand(String text, double fromScore, double toScore, ScoreTO score, Command command) {
		logger.info("You said: '" + text + "' (score " + score + ")");
		if (!enabled) {
			String msg = "Speech recognition is disabled";
			TrayControl.showMessage(msg);
			logger.info(msg);
		} else if (score.getSelectedScore() <= fromScore && score.getSelectedScore() >= toScore) {
			history.add(new SpeechLogTO(new Date(), text, score.getSelectedScore(), true));
			TrayControl.showMessage(text + " (score " + score + ")");
			logger.info("Score in range");
			// readyTime = System.currentTimeMillis();
			try {
				command.execute();
			} catch (Exception e) {
				String msg = "Command failed";
				logger.info(msg, e);
				TrayControl.showMessage(msg, MessageLevel.ERROR);
			}
		} else {
			logger.info("Score out of range");
			history.add(new SpeechLogTO(new Date(), text, score.getSelectedScore(), false));
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public static List<SpeechLogTO> getHistory() {
		return Collections.unmodifiableList(history);
	}
}
