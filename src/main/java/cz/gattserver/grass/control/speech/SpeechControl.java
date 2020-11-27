package cz.gattserver.grass.control.speech;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.gattserver.grass.control.CmdControl;
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

	private static final String GRASS_PLAYER_NEXT = "grass control player next";
	private static final String GRASS_PLAYER_PREVIOUS = "grass control player previous";
	private static final String GRASS_PLAYER_STOP = "grass control player stop";
	private static final String GRASS_PLAYER_PLAY = "grass control player play";

	private static final String PLAYER_NEXT = "player next";
	private static final String PLAYER_PREVIOUS = "player previous";
	private static final String PLAYER_STOP = "player stop";
	private static final String PLAYER_PLAY = "player play";
	private static final String PLAYER_VOLUME_UP = "player volume up";
	private static final String PLAYER_VOLUME_DOWN = "player volume down";
	private static final String PLAYER_RANDOM_ON = "player random on";
	private static final String PLAYER_RANDOM_OFF = "player random off";
	private static final String PLAYER_STATUS = "player status";

	private static final String OPEN_HW = "grass control open hardware";
	private static final String OPEN_GRASS = "grass control open grass";
	private static final String OPEN_NEXUS = "grass control open nexus";

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
					executeCommand(s, -2.40E8, -5.11E8, score, () -> VLCControl.sendCommand(VLCCommand.NEXT));
					break;
				case GRASS_PLAYER_PREVIOUS:
					executeCommand(s, -2.35E8, -4.76E8, score, () -> VLCControl.sendCommand(VLCCommand.NEXT));
					break;
				case GRASS_PLAYER_STOP:
					executeCommand(s, -2.30E8, -4.40E8, score, () -> VLCControl.sendCommand(VLCCommand.PAUSE));
					break;
				case GRASS_PLAYER_PLAY:
					executeCommand(s, -2.40E8, -4.10E8, score, () -> VLCControl.sendCommand(VLCCommand.PLAY));
					break;

				case PLAYER_NEXT:
					executeCommand(s, -1.40E8, -6.76E8, score, () -> VLCControl.sendCommand(VLCCommand.NEXT));
					break;
				case PLAYER_PREVIOUS:
					executeCommand(s, -1.50E8, -3.10E8, score, () -> VLCControl.sendCommand(VLCCommand.NEXT));
					break;
				case PLAYER_STOP:
					executeCommand(s, -1.50E8, -5.00E8, score, () -> VLCControl.sendCommand(VLCCommand.PAUSE));
					break;
				case PLAYER_PLAY:
					// false -1.40
					// true -1.51, -1.76
					executeCommand(s, -1.56E8, -5.00E8, score, () -> VLCControl.sendCommand(VLCCommand.PLAY));
					break;
				case PLAYER_VOLUME_UP:
					// false -1.98
					executeCommand(s, -1.90E8, -3.20E8, score, () -> VLCControl.sendCommand(VLCCommand.VOLUP));
					break;
				case PLAYER_VOLUME_DOWN:
					executeCommand(s, -1.36E8, -3.30E8, score, () -> VLCControl.sendCommand(VLCCommand.VOLDOWN));
					break;
				case PLAYER_RANDOM_ON:
					executeCommand(s, -1.36E8, -3.30E8, score, () -> VLCControl.sendCommand(VLCCommand.RANDOM_ON));
					break;
				case PLAYER_RANDOM_OFF:
					executeCommand(s, -1.36E8, -3.46E8, score, () -> VLCControl.sendCommand(VLCCommand.RANDOM_OFF));
					break;
				case PLAYER_STATUS:
					executeCommand(s, -1.36E8, -4.99E8, score, () -> VLCControl.sendCommand(VLCCommand.STATUS));
					break;

				case OPEN_HW:
					executeCommand(s, -2.40E8, -4.13E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz/hw"));
					break;
				case OPEN_GRASS:
					executeCommand(s, -2.40E8, -5.37E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz"));
					break;
				case OPEN_NEXUS:
					executeCommand(s, -2.59E8, -4.56E8, score,
							() -> CmdControl.openChrome("https://www.gattserver.cz:8843"));
					break;
				}
			}
		}
		recognizer.stopRecognition();
	}

	private void executeCommand(String text, double fromScore, double toScore, Float score, Command command) {
		if (!enabled) {
			String msg = "Speech recognition is disabled";
			TrayControl.showMessage(msg);
			logger.info(msg);
		} else if (score <= fromScore && score >= toScore) {
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
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
