package cz.gattserver.grass.control.speech;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.CmdControl;
import cz.gattserver.grass.control.TrayControl;
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

	private static final String PLAYER_NEXT = "grass control player next";
	private static final String PLAYER_NEXT_SHORTCUT = "player next";
	private static final String PLAYER_PREVIOUS = "grass control player previous";
	private static final String PLAYER_PREVIOUS_SHORTCUT = "player previous";
	private static final String PLAYER_STOP = "grass control player stop";
	private static final String PLAYER_PLAY = "grass control player play";
	private static final String HW = "grass control open hardware";
	private static final String GRASS = "grass control open grass";
	private static final String NEXUS = "grass control open nexus";

	private static final long TIMEOUT = 10000; // 10s

	private volatile boolean running = false;
	private volatile boolean enabled = true;

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
			LastAcceptedTO lastAccepted = null;
			while ((result = recognizer.getResult()) != null) {
				String s = result.getHypothesis();
				if ("<unk>".equals(s) || "".equals(s))
					continue;
				Token bestToken = result.getResult().getBestFinalToken();
				Float score = bestToken == null ? null : bestToken.getScore();

				// Vypadá to, že čím lepší frázování (oddělení slov při
				// zadávání, tím lepší skore)
				logger.info("You said: '" + s + "' (score " + score + ")");

				// Ani přes skore není možné odchytit správně (obrovské množství
				// false-positive, které svým rozsahem výskytu přesahují
				// true-positve)
				// v l c next
				// v l c previous
				// v l c stop
				// v l c play

				if (lastAccepted != null && System.currentTimeMillis() - lastAccepted.getTime() > TIMEOUT) {
					lastAccepted = null;
					String msg = "Player shortcut window closed";
					TrayControl.INSTANCE.showMessage(msg);
					logger.info(msg);
				}

				switch (s) {
				case PLAYER_NEXT:
					// false -2.40568032E8
					lastAccepted = executeCommand(s, -2.4E8, -5.11E8, score,
							() -> VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT));
					break;
				case PLAYER_NEXT_SHORTCUT:
					if (isInPlayerShortcutWindow(lastAccepted)) {
						// false -1.36 až -2.83
						// true -1.61 až -1.90
						lastAccepted = executeCommand(s, -1.5E8, -3.1E8, score,
								() -> VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT));
					}
					break;
				case PLAYER_PREVIOUS:
					// false
					lastAccepted = executeCommand(s, -2.35E8, -4.76348384E8, score,
							() -> VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT));
					break;
				case PLAYER_PREVIOUS_SHORTCUT:
					if (isInPlayerShortcutWindow(lastAccepted)) {
						// false -1.36 až -2.83
						// true -1.61 až -1.90
						lastAccepted = executeCommand(s, -1.5E8, -3.1E8, score,
								() -> VLCControl.INSTANCE.sendCommand(VLCCommand.NEXT));
					}
					break;
				case PLAYER_STOP:
					lastAccepted = executeCommand(s, -2.3E8, -4.1E8, score,
							() -> VLCControl.INSTANCE.sendCommand(VLCCommand.PAUSE));
					break;
				case PLAYER_PLAY:
					lastAccepted = executeCommand(s, -2.4E8, -3.67315744E8, score,
							() -> VLCControl.INSTANCE.sendCommand(VLCCommand.PLAY));
					break;
				case HW:
					// false -3.29825952E8 až -5.08478624E8
					lastAccepted = executeCommand(s, -2.4E8, -3.29E8, score,
							() -> CmdControl.INSTANCE.openChrome("https://www.gattserver.cz/hw"));
					break;
				case GRASS:
					// false -3.01723776E8 až -7.1960922E8
					// true -2.4 až 4.29 
					lastAccepted = executeCommand(s, -2.40E8, -3.2E8, score,
							() -> CmdControl.INSTANCE.openChrome("https://www.gattserver.cz"));
					break;
				case NEXUS:
					// false
					lastAccepted = executeCommand(s, -2.59455376E8, -4.56E8, score,
							() -> CmdControl.INSTANCE.openChrome("https://www.gattserver.cz:8843"));
					break;
				}
			}
		}
		recognizer.stopRecognition();
	}

	private boolean isInPlayerShortcutWindow(LastAcceptedTO to) {
		if (to == null)
			return false;
		switch (to.getText()) {
		case PLAYER_PLAY:
		case PLAYER_STOP:
		case PLAYER_NEXT:
		case PLAYER_NEXT_SHORTCUT:
		case PLAYER_PREVIOUS:
		case PLAYER_PREVIOUS_SHORTCUT:
			return true;
		default:
			return false;
		}
	}

	private LastAcceptedTO executeCommand(String text, double fromScore, double toScore, Float score, Command command) {
		if (!enabled) {
			String msg = "Speech recognition is disabled";
			TrayControl.INSTANCE.showMessage(msg);
			logger.info(msg);
			return null;
		}
		if (score <= fromScore && score >= toScore) {
			TrayControl.INSTANCE.showMessage(text + " (score " + score + ")");
			logger.info("Score in range");
			command.execute();
			return new LastAcceptedTO(text, System.currentTimeMillis());
		} else {
			logger.info("Score out of range");
			return null;
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
